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


import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpHost;

import java.io.InputStream;
import java.net.URI;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;

import static no.digipost.api.useragreements.client.AgreementType.FETCH_MESSAGES;

public class Examples {

	private DigipostUserAgreementsClient client;

	public void instantiate_client() {
		InputStream key = getClass().getResourceAsStream("certificate.p12");

		HttpHost proxy = new HttpHost("proxy.example.com", 8080);

		final BrokerId brokerId = BrokerId.of(1234L);

		DigipostUserAgreementsClient client = new DigipostUserAgreementsClient
				.Builder(brokerId, key, "password")
				.useProxy(proxy) //optional
				.setHttpClientBuilder(HttpClientBuilder.create()) //optional
				.serviceEndpoint(URI.create("https://api.digipost.no")) //optional
				.build();
	}

	public void identify_user() {
		final SenderId senderId = SenderId.of(1234L);
		final UserId userId = UserId.of("01017012345");

		final IdentificationResult identificationResult = client.identifyUser(senderId, userId);
		boolean isDigipost = identificationResult.getResult() == IdentificationResultCode.DIGIPOST;
	}

	public void crud_agreement() {
		final SenderId senderId = SenderId.of(1234L);
		final UserId userId = UserId.of("01017012345");

		//CreateAgreement
		client.createOrReplaceAgreement(senderId, new Agreement(FETCH_MESSAGES, userId, new HashMap<>()));

		//GetAgreement
		final GetAgreementResult agreement = client.getAgreement(senderId, FETCH_MESSAGES, userId);

		//UpdateAgreement
		client.createOrReplaceAgreement(senderId, new Agreement(FETCH_MESSAGES, userId, new HashMap<>()));

		//DeleteAgreement
		client.deleteAgreement(senderId, FETCH_MESSAGES, userId);
	}

	public void check_fetch_messages_agreement() {
		final SenderId senderId = SenderId.of(1234L);
		final UserId userId = UserId.of("01017012345");

		final GetAgreementResult agreementResult = client.getAgreement(senderId, FETCH_MESSAGES, userId);
		if (agreementResult.isSuccess()) {
			final Agreement agreement = agreementResult.getAgreement();
		} else {
			switch (agreementResult.getFailedReason()) {
				case UNKNOWN_USER: //User does not hav a Digipost account
				case NO_AGREEMENT: //No agreement exist for user
			}
		}
	}

	public void get_messages() {
		final SenderId senderId = SenderId.of(1234L);
		final UserId userId = UserId.of("01017012345");

		final List<Document> documents = client.getDocuments(senderId, FETCH_MESSAGES, userId, GetDocumentsQuery.empty());

		final ZoneId OSLO_ZONE = ZoneId.of("Europe/Oslo");
		final List<Document> allOptions = client.getDocuments(senderId, FETCH_MESSAGES, userId, GetDocumentsQuery.builder()
				.deliveryTimeFrom(ZonedDateTime.of(2020, 1, 1, 0, 0, 0, 0, OSLO_ZONE))
				.deliveryTimeTo(ZonedDateTime.now(OSLO_ZONE))
				.build());
	}
}
