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

import no.digipost.api.useragreements.client.response.StreamingRateLimitedResponse;
import no.digipost.api.useragreements.client.response.WithNextAllowedRequestTime;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static co.unruly.matchers.Java8Matchers.where;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class StreamingRateLimitedResponseTest {

	static final class ResponseElements implements WithNextAllowedRequestTime {

		private final int amount;
		private final Optional<Instant> nextAllowedRequest;

		public ResponseElements(int amount) {
			this(amount, null);
		}

		public ResponseElements(int amount, Instant nextAllowedRequest) {
			this.amount = amount;
			this.nextAllowedRequest = Optional.ofNullable(nextAllowedRequest);
		}

		public Stream<Integer> elements() {
			return IntStream.range(0, amount).boxed();
		}

		@Override
		public Optional<Instant> getNextAllowedRequestTime() {
			return nextAllowedRequest;
		}
	}

	@Test
	public void populatesNextAllowedRequestTimeWhenConsumingStream() {
		Instant nextAllowedRequest = Instant.now().plus(Duration.ofMinutes(10));
		StreamingRateLimitedResponse<String> response = new StreamingRateLimitedResponse<>(Stream.of(new ResponseElements(1), new ResponseElements(2, nextAllowedRequest)), ResponseElements::elements)
				.map(String::valueOf);
		try (Stream<String> responseStrings = response.asStream()) {
			assertThat(responseStrings.collect(toList()), contains("0", "0", "1"));
		}
		assertThat(response, where(StreamingRateLimitedResponse::getNextAllowedRequest, is(nextAllowedRequest)));
	}
}
