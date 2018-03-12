---
title: Bank account numbers
identifier: bankaccount-nums
layout: default
---

Users may allow access to their registered bank account numbers for specific senders.  The agreement type `BANK_ACCOUNT_NUMBER_FOR_RECEIPTS` allows a sender
to retrieve users' bank account numbers in order to couple the bank accounts with digital receipts to be sent to the users' Digipost inbox.
 The sending is performed with the regular API for sending documents to Digipost users: [digipost.github.io/digipost-api-client-java](http://digipost.github.io/digipost-api-client-java/)



### Fetch all bank account numbers

```java
import static no.digipost.api.useragreements.client.AgreementType.BANK_ACCOUNT_NUMBER_FOR_RECEIPTS;

...

// The ID of the receipt supplier party, i.e. the organization with access to bank account numbers
SenderId sender = new SenderId(1234L);

// The stream is made available immediately when the client starts receiving the response,
// which is a rather long chunked http response. The processing of the stream should
// continuously persist/update the received agreements.
try (Stream<Agreement> accounts = client.getAgreementsOfType(sender, BANK_ACCOUNT_NUMBER_FOR_RECEIPTS)) {
    accounts.forEach(account -> persistAccountNumber(account.getUserId().serialize()));
}

...

private void persistAccountNumber(String accountNumber) {
    // handle persisting or updating the received account number
}
```



### Send receipt to user with bank account number

This is performed with the [Digipost API Client library](http://digipost.github.io/digipost-api-client-java).

