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

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXB;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.Collections;

import static org.hamcrest.core.Is.is;

public class MarshallingTest {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Test
	public void shouldMarshallUnmarshallAgreement() throws URISyntaxException {
		final StringWriter xml = new StringWriter();
		final UserId userId = UserId.of("01017012345");
		final Agreement agreement = Agreement.createInvoiceBankAgreement(userId, true);
		JAXB.marshal(agreement, xml);
		log.debug(xml.toString());
		final Agreement unmarshal = JAXB.unmarshal(new StringReader(xml.toString()), Agreement.class);
		Assert.assertThat(unmarshal.getUserId(), is(userId));
	}

	@Test
	public void shouldMarshallUnmarshallAgreements() {
		final StringWriter xml = new StringWriter();
		final Agreements agreements = new Agreements(Collections.singletonList(Agreement.createInvoiceBankAgreement(UserId.of("01017012345"), true)));
		JAXB.marshal(agreements, xml);
		log.debug(xml.toString());
		JAXB.unmarshal(new StringReader(xml.toString()), Agreements.class);
	}
}
