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

import jakarta.xml.bind.JAXB;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;

import static co.unruly.matchers.OptionalMatchers.contains;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class MarshallingTest {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Test
	public void shouldMarshallUnmarshallAgreement() throws URISyntaxException {
		final StringWriter xml = new StringWriter();
		final UserId userId = UserId.of("01017012345");
		final Agreement agreement = new Agreement(AgreementType.FETCH_MESSAGES, userId, new HashMap<>());
		JAXB.marshal(agreement, xml);
		log.debug(xml.toString());
		final Agreement unmarshal = JAXB.unmarshal(new StringReader(xml.toString()), Agreement.class);
		assertThat(unmarshal.getUserId(), is(userId));
	}

	@Test
	public void shouldMarshallUnmarshallAgreements() {
		final StringWriter xml = new StringWriter();
		final UserId userId = UserId.of("01017012345");
		final Agreement agreement = new Agreement(AgreementType.FETCH_MESSAGES, userId, new HashMap<>());
		final Agreements agreements = new Agreements(singletonList(agreement));
		JAXB.marshal(agreements, xml);
		log.debug(xml.toString());
		JAXB.unmarshal(new StringReader(xml.toString()), Agreements.class);
	}

	@Test
	public void shouldMarshallUnmarshallAgreementOwners() {
		StringWriter xml = new StringWriter();
		List<UserId> accountNumbers = asList(UserId.of("180020111111"), UserId.of("180020111112"));
		Duration delay = Duration.ofSeconds(42);
		AgreementOwners owners = new AgreementOwners(accountNumbers, delay);
		JAXB.marshal(owners, xml);
		log.debug(xml.toString());
		AgreementOwners unmarshalled = JAXB.unmarshal(new StringReader(xml.toString()), AgreementOwners.class);
		assertThat(unmarshalled.getDelayUntilNextAllowedRequest(), contains(delay));
		assertThat(unmarshalled.getIds(), is(accountNumbers));
	}
}
