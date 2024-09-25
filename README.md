# Iron Verifiable Credentials Processor

An implementation of the [W3C Verifiable Credentials](https://www.w3.org/TR/vc-data-model/) model and processing in Java.

[![Java 17 CI](https://github.com/filip26/iron-verifiable-credentials/actions/workflows/java17-build.yml/badge.svg)](https://github.com/filip26/iron-verifiable-credentials/actions/workflows/java17-build.yml)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/806688cdb1d248e8b5cc2a67f6c2f0f8)](https://app.codacy.com/gh/filip26/iron-verifiable-credentials/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
[![Codacy Badge](https://app.codacy.com/project/badge/Coverage/806688cdb1d248e8b5cc2a67f6c2f0f8?branch=main)](https://app.codacy.com/gh/filip26/iron-verifiable-credentials/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_coverage)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=filip26_iron-verifiable-credentials&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=filip26_iron-verifiable-credentials)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=filip26_iron-verifiable-credentials&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=filip26_iron-verifiable-credentials)
[![Maven Central](https://img.shields.io/maven-central/v/com.apicatalog/iron-verifiable-credentials.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:com.apicatalog%20AND%20a:iron-verifiable-credentials)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Features

* Issuer, Verifier, Holder
* Signature Suites
  * [W3C Data Integrity Cryptosuites](https://www.w3.org/TR/vc-data-integrity/)
    * :package: [ECDSA-SD-2023](https://github.com/filip26/iron-ecdsa-sd-2023) [selective disclosure]
    * :package: [EdDSA-RDFC-2022](https://github.com/filip26/iron-eddsa-rdfc-2022)
    * :package: [ECDSA-RDFC-2019](https://github.com/filip26/iron-ecdsa-rdfc-2019) [P-256, P-384]
    * :package: [BBS-2023](https://github.com/filip26/iron-bbs-cryptosuite-2023) (planned)
  * :package: [Ed25519Signature2020](https://github.com/filip26/iron-ed25519-cryptosuite-2020)
  * Have you implemented a signature suite? List it here, open PR.
* Status Verification
  * [Bitstring Status List](https://www.w3.org/TR/vc-bitstring-status-list/)
* Data Models
  * [v2.0](https://www.w3.org/TR/vc-data-model-2.0)
  * [v1.1](https://www.w3.org/TR/vc-data-model-1.1)

[Community compatibility dashboard for Verifiable Credentials](https://canivc.com/)

## Usage

This repository provides common logic and primitives to easily implement a signature suite. It is intended to be used together with a suite, or suites, of your choice, e.g. [ECDSA-SD-2023](https://github.com/filip26/iron-ecdsa-sd-2023). Read the suite(s) documentation for specifics.

### Verifier

```javascript
// create a new verifier instance
static Verifier VERIFIER = Verifier.with(SUITE1, SUITE2, ...)
    // options
    .base(...)
    .loader(...)
    .useBundledContexts(true|false)
    .statusValidator(...)
    .subjectValidator(...)
    // ...
    ; 

try {
  // verify the given input proof(s)
  var verifiable = VERIFIER.verify(credential|presentation);
  
  // or with runtime parameters e.g. domain, challenge, etc.
  var verifiable = VERIFIER.verify(credential|presentation, parameters);
  
  // get verified details
  verifiable.subject()
  verifiable.id()
  verifiable.type()
  // ...
  
} catch (VerificationError | DocumentError e) {
  ...
}

```

### Issuer

```javascript
// create a new issuer instance
Issuer ISSUER = SUITE.createIssuer(keyPairProvider)
    // options
    .base(...)
    .loader(...)
    .useBundledContexts(true|false)
    // ...
    ; 

try {
  // issue a new verifiable, i.e. sign the input and add a new proof
  var verifiable = ISSUER.sign(credential|presentation, proofDraft).compacted();
  
} catch (SigningError | DocumentError e) {
  ...
}
```

### Holder

```javascript
// create a new holder instance
static Holder HOLDER = Holder.with(SUITE1, SUITE2, ...)
    // options
    .base(...)
    .loader(...)
    .useBundledContexts(true|false)
    // ...
    ; 

try {
  // derive a new signed credentials disclosing selected claims only
  var verifiable = HOLDER.derive(credential, selectors).compacted();

} catch (SigningError | DocumentError e) {
  ...
}
```

## Documentation

![Verifiable Credentials Signature Verification Data Flow](/doc/iron-vc-signature-verification-data-flow-v1.0.6.png)

![Verifiable Credentials Core Data Model](/doc/iron-vc-core-data-model-v1.0.3.png)

[![javadoc](https://javadoc.io/badge2/com.apicatalog/iron-verifiable-credentials/javadoc.svg)](https://javadoc.io/doc/com.apicatalog/iron-verifiable-credentials)



## Installation

### Maven
Java 17+

```xml
<dependency>
    <groupId>com.apicatalog</groupId>
    <artifactId>iron-verifiable-credentials</artifactId>
    <version>0.14.0</version>
</dependency>

```

Iron VC SDK for Android is distributed under a commercial license. [Contact](mailto:filip26@gmail.com)

## Contributing

All PR's welcome!

### Building

Fork and clone the project repository.

#### Java 17
```bash
> cd iron-verfiable-credentials
> mvn clean package
```

## Extensions
* :package: [Iridium](https://github.com/filip26/iridium-cbor-ld) - A CBOR-based Processor for Linked Data
* :gear: [VC HTTP API & Service](https://github.com/filip26/iron-vc-api)
* :hammer_and_wrench: [LEXREX](https://lexrex.web.app/) - Semantic vocabularies visual builder and manager

## Resources

* [W3C Verifiable Credentials Data Model v2.0](https://www.w3.org/TR/vc-data-model-2.0)
* [W3C Verifiable Credentials Data Model v1.1](https://www.w3.org/TR/vc-data-model-1.1)
* [W3C Verifiable Credential Data Integrity v1.0](https://www.w3.org/TR/vc-data-integrity)
* [W3C Bitstring Status List](https://www.w3.org/TR/vc-bitstring-status-list/) 
* [W3C Decentralized Identifiers (DIDs) v1.0](https://www.w3.org/TR/did-core/)
* [W3C Controller Documents v1.0](https://www.w3.org/TR/controller-document)
* [The did:key Method v0.7](https://w3c-ccg.github.io/did-method-key/)
* [Community compatibility dashboard for Verifiable Credentials](https://canivc.com/)
* [VC Playground](https://vcplayground.org/)

## Sponsors

<a href="https://github.com/digitalbazaar">
  <img src="https://avatars.githubusercontent.com/u/167436?s=200&v=4" width="40" />
</a> 

## Commercial Support
Commercial support is available at filip26@gmail.com
