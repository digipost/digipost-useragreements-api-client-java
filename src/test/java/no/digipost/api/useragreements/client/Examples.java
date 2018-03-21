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

import org.apache.http.HttpHost;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.InputStream;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import static no.digipost.api.useragreements.client.AgreementType.INVOICE_BANK;

public class Examples {

	private DigipostUserAgreementsClient client;

	public void instantiate_client() {
		InputStream key = getClass().getResourceAsStream("certificate.p12");

		HttpHost proxy = new HttpHost("proxy.example.com", 8080, "http");

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
		client.createOrReplaceAgreement(senderId, Agreement.createInvoiceBankAgreement(userId, false));

		//GetAgreement
		final GetAgreementResult agreement = client.getAgreement(senderId, INVOICE_BANK, userId);

		//UpdateAgreement
		client.createOrReplaceAgreement(senderId, Agreement.createInvoiceBankAgreement(userId, true));

		//DeleteAgreement
		client.deleteAgreement(senderId, INVOICE_BANK, userId);
	}

	public void check_invoice_agreement() {
		final SenderId senderId = SenderId.of(1234L);
		final UserId userId = UserId.of("01017012345");

		final GetAgreementResult agreementResult = client.getAgreement(senderId, INVOICE_BANK, userId);
		if (agreementResult.isSuccess()) {
			final Agreement agreement = agreementResult.getAgreement();
		} else {
			switch (agreementResult.getFailedReason()) {
				case UNKNOWN_USER: //User does not hav a Digipost account
				case NO_AGREEMENT: //No agreement exist for user
			}
		}
	}

	public void get_invoices() {
		final SenderId senderId = SenderId.of(1234L);
		final UserId userId = UserId.of("01017012345");

		final List<Document> unpaidInvoice = client.getDocuments(senderId, INVOICE_BANK, userId, GetDocumentsQuery.empty());

		final List<Document> allOptions = client.getDocuments(senderId, INVOICE_BANK, userId, GetDocumentsQuery.builder()
				.invoiceStatus(InvoiceStatus.PAID)
				.invoiceDueDateFrom(LocalDate.of(2017, 1, 1))
				.invoiceDueDateTo(LocalDate.of(2017, 5, 1))
				.build());
	}

	public void update_invoice_status() {
		final SenderId senderId = SenderId.of(1234L);
		final UserId userId = UserId.of("01017012345");

		final List<Document> unpaidInvoice = client.getDocuments(senderId, INVOICE_BANK, userId, GetDocumentsQuery.empty());
		final Document invoice = unpaidInvoice.get(0);

		//set status to PAID
		client.payInvoice(senderId, INVOICE_BANK, invoice.getId(), new InvoicePayment(123));

		//set status to DELETED
		client.deleteInvoice(senderId, INVOICE_BANK, invoice.getId());

		//generic version
		client.updateInvoice(senderId, INVOICE_BANK, invoice.getId(), new InvoiceUpdate(InvoiceStatus.PAID, 123));
	}
}
