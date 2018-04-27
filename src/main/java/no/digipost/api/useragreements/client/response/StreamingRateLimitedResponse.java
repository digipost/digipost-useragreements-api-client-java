/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.digipost.api.useragreements.client.response;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Represents a streamed response from the Digipost API, and offer mechanisms for consuming
 * it incrementally. In addition, one should (must) inspect {@link #getDelayUntilNextAllowedRequest()}
 * after consuming the response and not make any new request against the API resource until
 * this duration of time has passed.
 *
 * @param <T> The type of the entities contained in the response.
 */
public class StreamingRateLimitedResponse<T> {

	@FunctionalInterface
	public static interface ResponseElementHandler<T> {
		void handle(T id) throws Exception;
	}

	private final Stream<T> elements;
	private final AtomicBoolean consumed = new AtomicBoolean(false);
	private final Supplier<Duration> delayUntilNextAllowedRequest;

	public <S extends WithDelayUntilNextAllowedRequestTime> StreamingRateLimitedResponse(Stream<S> responseElements, Function<? super S, Stream<T>> flatMapper) {
		this(responseElements, new DeferredDelayUntilNextAllowedRequest(), flatMapper);
	}

	private <S extends WithDelayUntilNextAllowedRequestTime> StreamingRateLimitedResponse(Stream<S> responseElements, DeferredDelayUntilNextAllowedRequest deferredNextAllowedRequest, Function<? super S, Stream<T>> flatMapper) {
		this(responseElements.flatMap(elements -> {
			deferredNextAllowedRequest.register(elements);
			return flatMapper.apply(elements);
		}), deferredNextAllowedRequest);
	}

	public StreamingRateLimitedResponse(Stream<T> elements, Supplier<Duration> delayUntilNextAllowedRequest) {
		this.elements = elements;
		this.delayUntilNextAllowedRequest = delayUntilNextAllowedRequest;
	}

	public <R> StreamingRateLimitedResponse<R> map(Function<? super T, R> mapper) {
		return new StreamingRateLimitedResponse<>(asStream().map(mapper), delayUntilNextAllowedRequest);
	}

	public <R> StreamingRateLimitedResponse<R> flatMap(Function<? super T, Stream<R>> mapper) {
		return new StreamingRateLimitedResponse<>(asStream().flatMap(mapper), delayUntilNextAllowedRequest);
	}

	/**
	 * Consume each element of the response. This will properly terminate the response
	 * and close associated resources before the method returns, as opposed to {@link #asStream()}
	 * where you must handle this yourself. When this method returns, it is thus also safe to
	 * invoke {@link #getDelayUntilNextAllowedRequest()}, as this instant should have been captured from the
	 * end of the streaming response.
	 *
	 * @param handler The function to invoke for each element.
	 */
	public void forEach(ResponseElementHandler<T> handler) {
		try (Stream<T> autoClosed = asStream()) {
			elements.forEach(id -> {
				try {
					handler.handle(id);
				} catch (RuntimeException e) {
					throw e;
				} catch (Exception e) {
					throw new RuntimeException(e.getClass().getSimpleName() + ": '" + e.getMessage() + "'", e);
				}
			});
		}
	}

	/**
	 * Get the duration until a new request is allowed against the API resource which
	 * returned this response. If a new request is made before waiting this duration,
	 * the API will respond with an error, and requiring a new duration to wait before
	 * a new request will be allowed.
	 * <p>
	 * This method <em>must</em> be invoked <em>after</em> the response has been consumed
	 * e.g. using {@link #forEach(ResponseElementHandler)}, or acquiring the underlying
	 * {@link #asStream() stream} and properly consuming that with
	 * {@code try-with-resources}.
	 *
	 * @return the durtaion until a new request is allowed against the API resource.
	 * @throws NextAllowedRequestTimeNotFoundException if the duration has not yet been captured
	 *         from the response.
	 */
	public Duration getDelayUntilNextAllowedRequest() {
		Duration delay = delayUntilNextAllowedRequest.get();
		if (delay == null) {
			throw new NextAllowedRequestTimeNotFoundException();
		}
		return delay;
	}

	/**
	 * Expose the retrieved elements as a Java {@link Stream}. Using this method
	 * requires proper resource handling and the stream <strong>must</strong>
	 * be {@link Stream#close() closed} after it has been consumed.
	 * <p>
	 * Prefer {@link #forEach(ResponseElementHandler)} over this method.
	 *
	 * @return the elements of the response as a Stream.
	 */
	public Stream<T> asStream() {
		switchToConsumedState();
		return elements;
	}

	private void switchToConsumedState() {
		if (consumed.getAndSet(true)) {
			throw new IllegalStateException("This response is already consumed, and the invoked operation is illegal.");
		}

	}


	private static final class DeferredDelayUntilNextAllowedRequest implements Supplier<Duration> {
		private volatile Duration delayUntilNextAllowedRequest;

		void register(WithDelayUntilNextAllowedRequestTime element) {
			element.getDelayUntilNextAllowedRequest().ifPresent(delay -> this.delayUntilNextAllowedRequest = delay);
		}

		@Override
		public Duration get() {
			return delayUntilNextAllowedRequest;
		}
	}
}
