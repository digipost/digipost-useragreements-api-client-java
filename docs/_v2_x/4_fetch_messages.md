---
title: Messages sent to user
identifier: fetch_messages
layout: default
---

The agreement type `FETCH_MESSAGES` allows a sender to retrieve metadata for
documents that sender has previously sent to the user, that e.g. can be used to
present a synthetic inbox to the user. The metadata includes a deep-link the
user can use to access the document in Digipost.


### Create, read, update and delete agreement

```java
final SenderId senderId = SenderId.of(1234L);
final UserId userId = UserId.of("01017012345");

//CreateAgreement
client.createOrReplaceAgreement(senderId, Agreement.createAgreement(userId, AgreementType.FETCH_MESSAGES));

//GetAgreement
final GetAgreementResult agreement = client.getAgreement(senderId, AgreementType.FETCH_MESSAGES, userId);

//UpdateAgreement
client.createOrReplaceAgreement(senderId, Agreement.createAgreement(userId, AgreementType.FETCH_MESSAGES));

//DeleteAgreement
client.deleteAgreement(senderId, AgreementType.FETCH_MESSAGES, userId);
```

### Get and verify  agreement

```java
final SenderId senderId = SenderId.of(1234L);
final UserId userId = UserId.of("01017012345");

final GetAgreementResult agreementResult = client.getAgreement(senderId, AgreementType.FETCH_MESSAGES, userId);
if (agreementResult.isSuccess()) {
	final Agreement agreement = agreementResult.getAgreement();
} else {
	switch (agreementResult.getFailedReason()) {
		case UNKNOWN_USER: //User does not have a Digipost account
		case NO_AGREEMENT: //No agreement exists for user
	}
}
```

### Get documents

```java
final SenderId senderId = SenderId.of(1234L);
final UserId userId = UserId.of("01017012345");

final List<Document> unpaidInvoice = client.getDocuments(senderId, AgreementType.FETCH_MESSAGES, userId,
	GetDocumentsQuery.empty());

final ZoneId OSLO_ZONE = ZoneId.of("Europe/Oslo");
final List<Document> allOptions = client.getDocuments(senderId, AgreementType.FETCH_MESSAGES, userId,
	GetDocumentsQuery.builder()
		.deliveryTimeFrom(ZonedDateTime.of(2020, 1, 1, 0, 0, 0, 0, OSLO_ZONE))
		.deliveryTimeTo(ZonedDateTime.now(OSLO_ZONE))
		.build());
```
