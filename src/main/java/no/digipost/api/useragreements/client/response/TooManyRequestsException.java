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

import no.digipost.api.useragreements.client.UserAgreementsApiException;

import java.time.Instant;
import java.util.Optional;

import static no.digipost.api.useragreements.client.ErrorCode.CLIENT_TECHNICAL_ERROR;

public class TooManyRequestsException extends UserAgreementsApiException implements WithNextAllowedRequestTime {

	private final Instant nextAllowedRequest;

	public TooManyRequestsException(Optional<Instant> nextAllowedRequest) {
		super(CLIENT_TECHNICAL_ERROR, "This API resource has a rate limiter, and you are accessing it more frequent than it allows. " +	nextAllowedRequest
				.map(instant -> "Next request should be done not earlier than " + instant + ".")
				.orElse("There is no indication when you may try again."));
		this.nextAllowedRequest = nextAllowedRequest.orElse(null);
	}

	@Override
	public Optional<Instant> getNextAllowedRequestTime() {
		return Optional.ofNullable(nextAllowedRequest);
	}

}
