---
title: Invoice
identifier: invoice
layout: default
---

This API makes it possible for third parties (sender) to access and perform certain operations on a user's documents in Digipost.
The user grants the sender access to documents through an agreement. The agreement governs which documents the sender can access and what operations it can perform.
For example, the agreement type INVOICE_BANK allows a sender to retrieve a user's invoices and update their payment status.

[API specification](https://github.com/digipost/invoice-api-specification/blob/master/user-documents.md)

### Instantiate and configure client

```java
InputStream key = getClass().getResourceAsStream("certificate.p12");

HttpHost proxy = new HttpHost("proxy.example.com", 8080, "http");

final BrokerId brokerId = new BrokerId(1234L);

DigipostUserAgreementsClient client = new DigipostUserAgreementsClient
		.Builder(brokerId, key, "password")
		.useProxy(proxy) 					//optional
		.setHttpClientBuilder(HttpClientBuilder.create())	//optional
		.serviceEndpoint(URI.create("https://api.digipost.no")) //optional
		.build();
```

### Identify Digipost user

```java
final SenderId senderId = new SenderId(1234L);
final UserId userId = new UserId("01017012345");

final IdentificationResult identificationResult = client.identifyUser(senderId, userId);
boolean isDigipost = identificationResult.getResult() == IdentificationResultCode.DIGIPOST;
```

### Create, read, update and delete agreement

```java
final SenderId senderId = new SenderId(1234L);
final UserId userId = new UserId("01017012345");

//CreateAgreement
client.createOrReplaceAgreement(senderId, Agreement.createInvoiceBankAgreement(userId, false));

//GetAgreement
final GetAgreementResult agreement = client.getAgreement(senderId, INVOICE_BANK, userId);

//UpdateAgreement
client.createOrReplaceAgreement(senderId, Agreement.createInvoiceBankAgreement(userId, true));

//DeleteAgreement
client.deleteAgreement(senderId, INVOICE_BANK, userId);
```

### Get and verify invoice agreement

```java
final SenderId senderId = new SenderId(1234L);
final UserId userId = new UserId("01017012345");

final GetAgreementResult agreementResult = client.getAgreement(senderId, INVOICE_BANK, userId);
if (agreementResult.isSuccess()) {
	final Agreement agreement = agreementResult.getAgreement();
} else {
	switch (agreementResult.getFailedReason()) {
		case UNKNOWN_USER: //User does not hav a Digipost account
		case NO_AGREEMENT: //No agreement exist for user
	}
}
```

### Get invoices

```java
final SenderId senderId = new SenderId(1234L);
final UserId userId = new UserId("01017012345");

final List<Document> unpaidInvoice = client.getDocuments(senderId, INVOICE_BANK, userId,
	GetDocumentsQuery.empty());

final List<Document> allOptions = client.getDocuments(senderId, INVOICE_BANK, userId,
	GetDocumentsQuery.builder()
		.invoiceStatus(InvoiceStatus.PAID)
		.invoiceDueDateFrom(new LocalDate(2017, 1, 1))
		.invoiceDueDateTo(new LocalDate(2017, 5, 1))
```

### Update invoice status

```java
final SenderId senderId = new SenderId(1234L);
final UserId userId = new UserId("01017012345");

final List<Document> unpaidInvoice = client.getDocuments(senderId, INVOICE_BANK, userId, GetDocumentsQuery.empty());
final Document invoice = unpaidInvoice.get(0);

//set status to PAID
client.payInvoice(senderId, INVOICE_BANK, invoice.getId(), new InvoicePayment(123));

//set status to DELETED
client.deleteInvoice(senderId, INVOICE_BANK, invoice.getId());

//generic version
client.updateInvoice(senderId, INVOICE_BANK, invoice.getId(), new InvoiceUpdate(InvoiceStatus.PAID, 123));
```
