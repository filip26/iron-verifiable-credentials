# Iron Verifiable Credentials Processor & API

An implementation of the [Verifiable Credentials](https://www.w3.org/TR/vc-data-model/) in Java.

[![Java 17 CI](https://github.com/filip26/iron-verifiable-credentials/actions/workflows/java17-build.yml/badge.svg)](https://github.com/filip26/iron-verifiable-credentials/actions/workflows/java17-build.yml)
[![Android (Java 8) CI](https://github.com/filip26/iron-verifiable-credentials/actions/workflows/java8-build.yml/badge.svg)](https://github.com/filip26/iron-verifiable-credentials/actions/workflows/java8-build.yml)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/806688cdb1d248e8b5cc2a67f6c2f0f8)](https://www.codacy.com/gh/filip26/iron-verifiable-credentials/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=filip26/iron-verifiable-credentials&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://app.codacy.com/project/badge/Coverage/806688cdb1d248e8b5cc2a67f6c2f0f8)](https://www.codacy.com/gh/filip26/iron-verifiable-credentials/dashboard?utm_source=github.com&utm_medium=referral&utm_content=filip26/iron-verifiable-credentials&utm_campaign=Badge_Coverage)
[![Maven Central](https://img.shields.io/maven-central/v/com.apicatalog/iron-verifiable-credentials.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.apicatalog%22%20AND%20a:%22iron-verifiable-credentials%22)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Features

  * Verifying VC/VP
  * Issuing VC/VP
  * Signature
    * Ed25519 Signature 2020
  * [HTTP API & Service](https://github.com/filip26/vc-http-api)


## Usage

### Verifying 

```java

try {
  Vc.verify(credential|presentation)
  
    // optional options
    .domain(...)
    .base(...)
    .loader(documentLoader) 
    .statusVerifier(...)
    .didResolver(...)  
    .useBundledContexts(true|false)

    // assert valid document
    .isValid();
    
} catch (VerificationError | DataError e) {
  ...
}
```

### Issuing

```java
signed = Vc.sign(credential|presentation, keys, proofOptions)

           // optional options
           .base(...)
           .loader(documentLoader) 
           .useBundledContexts(true|false)
           
           // returns signed document in expanded form
           .getExpanded(); 

signed = Vc.sign(credential|presentation, keys, proofOptions)

           // returns signed document in compacted form
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
  * [Verifiable Credentials Data Model v1.1](https://www.w3.org/TR/vc-data-model/)
  * [Verifiable Credentials Use Cases](https://www.w3.org/TR/vc-use-cases/)
  * [Verifiable Credentials Implementation Guidelines 1.0](https://www.w3.org/TR/vc-imp-guide/)
  * [Data Integrity 1.0](https://w3c-ccg.github.io/data-integrity-spec/)
  * [Ed25519 Signature 2020](https://w3c-ccg.github.io/lds-ed25519-2020/)
  * [The did:key Method v0.7](https://w3c-ccg.github.io/did-method-key/)
  * [Decentralized Identifiers (DIDs) v1.0](https://www.w3.org/TR/did-core/)
