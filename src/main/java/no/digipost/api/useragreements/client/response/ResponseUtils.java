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

import no.digipost.api.useragreements.client.Error;
import no.digipost.api.useragreements.client.ErrorCode;
import no.digipost.api.useragreements.client.Headers;
import no.digipost.api.useragreements.client.RuntimeIOException;
import no.digipost.api.useragreements.client.UnexpectedResponseException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXB;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;
import static no.digipost.api.useragreements.client.ErrorCode.MULTIPLE_ENTITIES;
import static no.digipost.api.useragreements.client.ErrorCode.NO_ENTITY;

public final class ResponseUtils {

	private static final Logger RESPONSE_PAYLOAD_LOG = LoggerFactory.getLogger("no.digipost.api.useragreements.client.response_payload");

	private static final Pattern startOfNewXmlDocument = Pattern.compile("(?<=\\>)\\s*(?=\\<\\?[xX][mM][lL])");


	public static <T> T mapOkResponseOrThrowException(HttpResponse response, Function<HttpResponse, T> okResponseMapper) {
		final StatusLine statusLine = response.getStatusLine();
		if (isOkResponse(statusLine.getStatusCode())) {
			return okResponseMapper.apply(response);
		} else if (statusLine.getStatusCode() == 429) { // Too Many Requests
			TooManyRequestsException tooManyRequests;
			try {
				tooManyRequests = parseDelayDurationOfRetryAfterHeader(response)
						.map(TooManyRequestsException::new)
						.orElseGet(TooManyRequestsException::new);
			} catch (Exception e) {
				tooManyRequests = new TooManyRequestsException();
				tooManyRequests.addSuppressed(e);
			}
			throw tooManyRequests;
		} else {
			throw new UnexpectedResponseException(statusLine, readErrorEntity(response));
		}
	}

	public static <T> T unmarshallEntity(final HttpResponse response, final Class<T> returnType) {
		try (Stream<T> entityStream = unmarshallEntities(response, returnType)) {
			Iterator<T> entityIterator = entityStream.limit(2).iterator();
			if (!entityIterator.hasNext()) {
				throw new UnexpectedResponseException(response.getStatusLine(), NO_ENTITY,
						"Message body is empty");
			}
			T theEntity = entityIterator.next();
			if (entityIterator.hasNext()) {
				throw new UnexpectedResponseException(response.getStatusLine(), MULTIPLE_ENTITIES,
						"Message body contained more than one entity. First: " + theEntity + ", first excess one: " + entityIterator.next());
			}
			return theEntity;
		}
	}




	public static <T> Stream<T> unmarshallEntities(final HttpResponse response, final Class<T> returnType) {
		return streamXmlDocumentsOf(getResponseEntityContent(response))
				.peek(RESPONSE_PAYLOAD_LOG::trace)
				.map(xml -> {
					try {
						return JAXB.unmarshal(new ByteArrayInputStream(xml.getBytes()), returnType);
					} catch (IllegalStateException | DataBindingException e) {
						throw new UnexpectedResponseException(response.getStatusLine(), ErrorCode.GENERAL_ERROR, xml, e);
					}
				});
	}

	public static Error readErrorEntity(final HttpResponse response) {
		return unmarshallEntity(response, Error.class);
	}

	public static InputStream getResponseEntityContent(HttpResponse response) {
		Optional<CloseableHttpResponse> closeableResponse = Optional.of(response).filter(r -> r instanceof CloseableHttpResponse).map(r -> (CloseableHttpResponse) r);
		StatusLine statusLine = response.getStatusLine();
		HttpEntity entity = response.getEntity();
		if (entity == null) {
			closeableResponse.flatMap(r -> close(r)).map(RuntimeIOException::from).ifPresent(e -> { throw e; });
			return null;
		}

		try {
			return entity.getContent();
		} catch (UnsupportedOperationException | IOException e) {
			UnexpectedResponseException mainException = new UnexpectedResponseException(statusLine, ErrorCode.GENERAL_ERROR, e.getMessage(), e);
			closeableResponse.flatMap(r -> close(r)).ifPresent(mainException::addSuppressed);
			throw mainException;
		}
	}


	public static Stream<String> streamXmlDocumentsOf(InputStream inputStream) {
		return streamDelimitedStringsOf(inputStream, UTF_8, startOfNewXmlDocument).filter(chunk -> !chunk.trim().isEmpty());
	}

	public static Stream<String> streamDelimitedStringsOf(InputStream inputStream, Charset charset, Pattern delimiter) {
		Scanner contentScanner = new Scanner(inputStream, charset.name());
		return stream(spliteratorUnknownSize(contentScanner.useDelimiter(delimiter), IMMUTABLE), false)
				.onClose(() -> close(contentScanner, inputStream).map(RuntimeIOException::from).ifPresent(exception -> { throw exception; }));
	}


	public static Optional<Exception> close(AutoCloseable ... closeables) {
		Exception exception = null;
		for (AutoCloseable closeable : closeables) {
			try (AutoCloseable autoClosed = closeable) {
				continue;
			} catch (Exception e) {
				if (exception == null) {
					exception = e;
				} else {
					exception.addSuppressed(e);
				}
			}
		}
		return Optional.ofNullable(exception);
	}

	public static Optional<Duration> parseDelayDurationOfRetryAfterHeader(HttpResponse response) {
		return parseDelayDurationOfRetryAfterHeader(response, Clock.systemUTC());
	}

	public static Optional<Duration> parseDelayDurationOfRetryAfterHeader(HttpResponse response, Clock clock) {
		return getValueOfFirstHeader(response, Headers.Retry_After)
				.map(retryAfterValue -> {
					try {
						long parsedSeconds = Long.parseLong(retryAfterValue);
						return Duration.ofSeconds(parsedSeconds);
					} catch (NumberFormatException secondsNotParseable) {
						try {
							Instant parsedInstant = RFC_1123_DATE_TIME.parse(retryAfterValue, Instant::from);
							return Duration.between(clock.instant(), parsedInstant);
						} catch (RuntimeException e) {
							e.addSuppressed(secondsNotParseable);
							throw e;
						}
					}
				});
	}

	public static Optional<String> getValueOfFirstHeader(HttpResponse response, String headerName) {
		return Optional.ofNullable(response.getFirstHeader(headerName)).flatMap(h -> Optional.ofNullable(h.getValue()));
	}


	public static boolean isOkResponse(HttpResponse response) {
		return isOkResponse(response.getStatusLine());
	}

	public static boolean isOkResponse(StatusLine status) {
		return isOkResponse(status.getStatusCode());
	}

	public static boolean isOkResponse(int statusCode) {
		return statusCode / 100 == 2;
	}

	private ResponseUtils() {}

}
