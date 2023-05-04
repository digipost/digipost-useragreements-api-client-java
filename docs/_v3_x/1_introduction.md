---
title: Introduction
identifier: introduction
layout: default
---

This API makes it possible to access agreements between Digipost users and third parties, and followingly perform certain operations which the user has granted the third party.
For instance, the user may permit that certain information may be provided to a third party in order to receive a service.  The user may also grant the sender access to documents
through an agreement. The agreement governs which documents the sender can access and what operations it can perform.


### Download

The library can be acquired from Maven Central Repository, using the dependency management tool of your choice.
For Maven you can use the following dependency:

```xml
<dependency>
    <groupId>no.digipost</groupId>
    <artifactId>digipost-useragreements-api-client-java</artifactId>
    <version>3.0.0</version>
</dependency>
```

### Prerequisites

The library requires *Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files for JDK/JRE* to be installed:
[www.oracle.com/technetwork/java/javase/downloads/index.html](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

Starting from *Java 8 build 152* the unlimited strength cryptography policy files are bundled with the JDK/JRE, and may be enabled by setting
the security property `security.policy` to `"unlimited"`. How this is set depends on how you deploy your application, but if done early enough,
i.e. *before* the JCE framework is initialized, it can be set programatically like this:

```java
Security.setProperty("crypto.policy", "unlimited"); // only effective on Java 8 b152 or newer
```

More details are available in the [Java 8u152 Release Notes](http://www.oracle.com/technetwork/java/javase/8u152-relnotes-3850503.html#JDK-8157561).


### Instantiate and configure client

```java
InputStream key = getClass().getResourceAsStream("certificate.p12");

HttpHost proxy = new HttpHost("proxy.example.com", 8080, "http");

BrokerId brokerId = BrokerId.of(1234L);

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

