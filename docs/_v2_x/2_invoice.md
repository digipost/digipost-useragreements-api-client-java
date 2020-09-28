---
title: Invoice
identifier: invoice
layout: default
---

The agreement type `INVOICE_BANK` allows a sender to retrieve a user's invoices and update their payment status.



### Create, read, update and delete agreement

```java
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
```

### Get and verify invoice agreement

```java
final SenderId senderId = SenderId.of(1234L);
final UserId userId = UserId.of("01017012345");

final GetAgreementResult agreementResult = client.getAgreement(senderId, INVOICE_BANK, userId);
if (agreementResult.isSuccess()) {
	final Agreement agreement = agreementResult.getAgreement();
} else {
	switch (agreementResult.getFailedReason()) {
		case UNKNOWN_USER: //User does not have a Digipost account
		case NO_AGREEMENT: //No agreement exists for user
	}
}
```

### Get invoices

```java
final SenderId senderId = SenderId.of(1234L);
final UserId userId = UserId.of("01017012345");

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
```
