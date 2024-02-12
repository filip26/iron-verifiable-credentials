# Iron Verifiable Credentials Processor

An implementation of the [Verifiable Credentials](https://www.w3.org/TR/vc-data-model/) model and processing in Java.

[![Java 17 CI](https://github.com/filip26/iron-verifiable-credentials/actions/workflows/java17-build.yml/badge.svg)](https://github.com/filip26/iron-verifiable-credentials/actions/workflows/java17-build.yml)
[![Android (Java 8) CI](https://github.com/filip26/iron-verifiable-credentials/actions/workflows/java8-build.yml/badge.svg)](https://github.com/filip26/iron-verifiable-credentials/actions/workflows/java8-build.yml)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/806688cdb1d248e8b5cc2a67f6c2f0f8)](https://www.codacy.com/gh/filip26/iron-verifiable-credentials/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=filip26/iron-verifiable-credentials&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://app.codacy.com/project/badge/Coverage/806688cdb1d248e8b5cc2a67f6c2f0f8)](https://www.codacy.com/gh/filip26/iron-verifiable-credentials/dashboard?utm_source=github.com&utm_medium=referral&utm_content=filip26/iron-verifiable-credentials&utm_campaign=Badge_Coverage)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=filip26_iron-verifiable-credentials&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=filip26_iron-verifiable-credentials)
[![Maven Central](https://img.shields.io/maven-central/v/com.apicatalog/iron-verifiable-credentials.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:com.apicatalog%20AND%20a:iron-verifiable-credentials)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Features

* Verifying VC/VP   
* Issuing VC/VP
* Verifiable Credentials Data Models
  * [v2.0](https://w3c.github.io/vc-data-model/)
  * [v1.1](https://www.w3.org/TR/vc-data-model/)
* Signature Suites
  * [Data Integrity 1.0 Proofs](https://w3c-ccg.github.io/data-integrity-spec/)
    * [ECDSA-2019](https://github.com/filip26/iron-ecdsa-cryptosuite-2019)
    * [EdDSA-RDFC-2022](https://github.com/filip26/iron-eddsa-rdfc-cryptosuite-2022)
    * [ECDSA-SD-2023](https://github.com/filip26/iron-ecdsa-sd-cryptosuite-2023) (planned)
    * [BBS-2023](https://github.com/filip26/iron-bbs-cryptosuite-2023) (planned)
  * [Ed25519Signature2020](https://github.com/filip26/iron-ed25519-cryptosuite-2020)
  * Have you implemented a signature provider? List it here, open PR.
* [VC HTTP API & Service](https://github.com/filip26/iron-vc-api)

## Extensions
* [Iridium](https://github.com/filip26/iridium-cbor-ld) - A CBOR-based Processor for Linked Data

## Installation

### Maven
Java 17+

```xml
<dependency>
    <groupId>com.apicatalog</groupId>
    <artifactId>iron-verifiable-credentials</artifactId>
    <version>0.8.1</version>
</dependency>

```

### Gradle
Android 12+ (API Level >=31)

```gradle
compile group: 'com.apicatalog', name: 'iron-verifiable-credentials-jre8', version: '0.8.1'
```

## Documentation

[![javadoc](https://javadoc.io/badge2/com.apicatalog/iron-verifiable-credentials/javadoc.svg)](https://javadoc.io/doc/com.apicatalog/iron-verifiable-credentials)

## Usage

Please use together with a cryptosuite(s) of your choice, e.g. [Ed25519Signature2020](https://github.com/filip26/iron-ed25519-cryptosuite-2020). Read the suite(s) documentation for specifics.

### Verifying 

```java

try {
  Vc.verify(credential|presentation, suites)
      
    // optional
    .base(...)
    .loader(documentLoader) 
    .statusVerifier(...)
    .useBundledContexts(true|false)

    // custom | suite specific | parameters
    .param(..., ....)

    // assert document validity
    .isValid();
    
} catch (VerificationError | DataError e) {
  ...
}

```

### Issuing

```java
Vc.sign(credential|presentation, keys, proofDraft)

   // optional
   .base(...)
   .loader(documentLoader) 
   .statusVerifier(...)
   .useBundledContexts(true|false)

   // return signed document in a compacted form
   .getCompacted(context);

```

## Contributing

All PR's welcome!

### Building

Fork and clone the project repository.

#### Java 17
```bash
> cd iron-verfiable-credentials
> mvn clean package
```

#### Java 8
```bash
> cd iron-verfiable-credentials
> mvn -f pom_jre8.xml clean package
```

## Resources
* [Verifiable Credentials Data Model v2.0](https://w3c.github.io/vc-data-model/)
* [Verifiable Credentials Data Model v1.1](https://www.w3.org/TR/vc-data-model/)
* [Verifiable Credentials Use Cases](https://www.w3.org/TR/vc-use-cases/)
* [Verifiable Credentials Implementation Guidelines 1.0](https://www.w3.org/TR/vc-imp-guide/)
* [Data Integrity 1.0](https://w3c-ccg.github.io/data-integrity-spec/)
* [The did:key Method v0.7](https://w3c-ccg.github.io/did-method-key/)
* [Decentralized Identifiers (DIDs) v1.0](https://www.w3.org/TR/did-core/)
* [VC v2.0 Interoperability Report](https://digitalbazaar.github.io/vc-data-model-2-test-suite/)

## Sponsors

<a href="https://github.com/digitalbazaar">
  <img src="https://avatars.githubusercontent.com/u/167436?s=200&v=4" width="40" />
</a> 

## Commercial Support
Commercial support is available at filip26@gmail.com
,
