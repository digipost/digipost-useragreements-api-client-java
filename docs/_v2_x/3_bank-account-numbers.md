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
SenderId sender = SenderId.of(1234L);


// The stream is made available immediately when the client starts receiving
// the response, which is a rather long chunked http response.
StreamingRateLimitedResponse<UserId> accountsResponse =
    client.getAgreementOwners(sender, BANK_ACCOUNT_NUMBER_FOR_RECEIPTS);


// The processing of the stream should
// continuously persist/update the received agreements.
accountsResponse.map(UserId::serialize).forEach(this::persistAccountNumber);


// Lastly, get the duration you must wait before you are allowed to do the request
// again, and ensure that any subsequent request does not happen before.
Duration delay = accountsResponse.getDelayUntilNextAllowedRequest();

...

private void persistAccountNumber(String accountNumber) {
    // handle persisting or updating the received account number
}
```

### Alternative: consume response with [Java Stream API](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html)

```java
// Alternatively, one can acquire the underlying Java Stream from the
// StreamingRateLimitedResponse, but it is imperative with proper resource
// management and that the stream is closed after it is consumed.
try (Stream<UserId> accounts = accountsResponse.asStream()) {
    accounts.map(UserId::serialize).forEach(this::persistAccountNumber);
}
```



### Send receipt to user with bank account number

This is performed with the [Digipost API Client library](http://digipost.github.io/digipost-api-client-java).

