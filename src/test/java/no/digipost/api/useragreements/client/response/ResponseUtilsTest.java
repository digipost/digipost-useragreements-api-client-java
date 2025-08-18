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

import no.digipost.api.useragreements.client.Headers;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.junit.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static co.unruly.matchers.Java8Matchers.where;
import static co.unruly.matchers.OptionalMatchers.contains;
import static co.unruly.matchers.OptionalMatchers.empty;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ResponseUtilsTest {

	@Test
	public void parsesSecondsFromRetryAfterHeaderIn429Response() {
		HttpResponse tooManyRequestsErrorResponse = tooManyRequestsResponseWithRetryAfter("10");
		try {
			ResponseUtils.mapOkResponseOrThrowException(tooManyRequestsErrorResponse, Function.identity());
			fail("should have thrown exception");
		} catch (TooManyRequestsException tooManyRequests) {
			assertThat(tooManyRequests, where(TooManyRequestsException::getDelayUntilNextAllowedRequest, contains(Duration.ofSeconds(10))));
			assertThat(tooManyRequests, where(Throwable::getSuppressed, emptyArray()));
		}
	}

	@Test
	public void parsesDateFromRetryAfterHeader() {
		Clock clock = Clock.fixed(Instant.now().truncatedTo(SECONDS), ZoneOffset.UTC);
		HttpResponse tooManyRequestsErrorResponse = tooManyRequestsResponseWithRetryAfter(RFC_1123_DATE_TIME.format(ZonedDateTime.now(clock).plusSeconds(42)));

		Optional<Duration> parsedDuration = ResponseUtils.parseDelayDurationOfRetryAfterHeader(tooManyRequestsErrorResponse, clock);
		assertThat(parsedDuration, contains(Duration.ofSeconds(42)));
	}


	@Test
	public void malformedRetryAfterHeaderIncludesSuppressedException() {
		HttpResponse tooManyRequestsErrorResponse = tooManyRequestsResponseWithRetryAfter("does not compute");
		try {
			ResponseUtils.mapOkResponseOrThrowException(tooManyRequestsErrorResponse, Function.identity());
			fail("should have thrown exception");
		} catch (TooManyRequestsException tooManyRequests) {
			assertThat(tooManyRequests, where(TooManyRequestsException::getDelayUntilNextAllowedRequest, empty()));
			List<Throwable> allSuppressed = Stream.of(tooManyRequests.getSuppressed()).flatMap(suppressed -> concat(Stream.of(suppressed), Stream.of(suppressed.getSuppressed()))).collect(toList());
			assertThat(allSuppressed, containsInAnyOrder(instanceOf(DateTimeParseException.class), instanceOf(NumberFormatException.class)));
		}
	}


	private static HttpResponse tooManyRequestsResponseWithRetryAfter(String retryAfterValue) {
		HttpResponse tooManyRequestsErrorResponse = new BasicHttpResponse(429);
		tooManyRequestsErrorResponse.addHeader(Headers.Retry_After, retryAfterValue);
		return tooManyRequestsErrorResponse;
	}
}
