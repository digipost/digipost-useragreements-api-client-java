---
title: Introduction
identifier: introduction
layout: default
---

This API makes it possible to access agreements between Digipost users and third parties, and followingly perform certain operations which the user has granted the third party.
For instance, the user may permit that certain information may be provided to a third party in order to receive a service.  The user may also grant the sender access to documents
through an agreement. The agreement governs which documents the sender can access and what operations it can perform.


### Instantiate and configure client

```java
InputStream key = getClass().getResourceAsStream("certificate.p12");

HttpHost proxy = new HttpHost("proxy.example.com", 8080, "http");

final BrokerId brokerId = BrokerId.of(1234L);

DigipostUserAgreementsClient client = new DigipostUserAgreementsClient
		.Builder(brokerId, key, "password")
		.useProxy(proxy) 					//optional
		.setHttpClientBuilder(HttpClientBuilder.create())	//optional
		.serviceEndpoint(URI.create("https://api.digipost.no")) //optional
		.build();
```

### Identify Digipost user

```java
final SenderId senderId = SenderId.of(1234L);
final UserId userId = UserId.of("01017012345");

final IdentificationResult identificationResult = client.identifyUser(senderId, userId);
boolean isDigipost = identificationResult.getResult() == IdentificationResultCode.DIGIPOST;
```

