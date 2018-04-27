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
package no.digipost.api.useragreements.client;

import no.digipost.api.useragreements.client.response.WithDelayUntilNextAllowedRequestTime;
import no.digipost.api.useragreements.client.xml.LongSecondsXmlAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "agreement-owners")
public class AgreementOwners implements WithDelayUntilNextAllowedRequestTime {

	@XmlElement(name = "id")
	private List<UserId> ids;

	@XmlElement(name = "seconds-until-next-allowed-request")
	@XmlJavaTypeAdapter(LongSecondsXmlAdapter.class)
	private Duration delayUntilNextAllowedRequest;

	public AgreementOwners(final List<UserId> users, Duration delayUntilNextAllowedRequest) {
		this.ids = users;
		this.delayUntilNextAllowedRequest = delayUntilNextAllowedRequest;
	}

	public List<UserId> getIds() {
		if (ids == null) {
			ids = new ArrayList<>();
		}
		return ids;
	}

	public Stream<UserId> getIdsAsStream() {
		return getIds().stream();
	}

	@Override
	public Optional<Duration> getDelayUntilNextAllowedRequest() {
		return Optional.ofNullable(delayUntilNextAllowedRequest);
	}

	@Override
	public String toString() {
		return ids.toString();
	}


	// Used by JAXB
	@SuppressWarnings("unused")
	private AgreementOwners() {}

}
