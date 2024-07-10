> [!IMPORTANT]
> Please consider a donation to keep the project available and maintained as an open-source.
> 
> The common experience with open-source is that no one wants to pay for it, but they will happily use it for free.
> -- Be different, be proud of it, brag about it, and feel good!

# Iron Verifiable Credentials Processor

An implementation of the [W3C Verifiable Credentials](https://www.w3.org/TR/vc-data-model/) model and processing in Java.

[![Java 17 CI](https://github.com/filip26/iron-verifiable-credentials/actions/workflows/java17-build.yml/badge.svg)](https://github.com/filip26/iron-verifiable-credentials/actions/workflows/java17-build.yml)
[![Java 11 CI](https://github.com/filip26/iron-verifiable-credentials/actions/workflows/java11-build.yml/badge.svg)](https://github.com/filip26/iron-verifiable-credentials/actions/workflows/java11-build.yml)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/806688cdb1d248e8b5cc2a67f6c2f0f8)](https://app.codacy.com/gh/filip26/iron-verifiable-credentials/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
[![Codacy Badge](https://app.codacy.com/project/badge/Coverage/806688cdb1d248e8b5cc2a67f6c2f0f8)](https://app.codacy.com/gh/filip26/iron-verifiable-credentials/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_coverage)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=filip26_iron-verifiable-credentials&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=filip26_iron-verifiable-credentials)
[![Maven Central](https://img.shields.io/maven-central/v/com.apicatalog/iron-verifiable-credentials.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:com.apicatalog%20AND%20a:iron-verifiable-credentials)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Features

* Issuer, Verifier, Holder
* Signature Suites
  * [W3C Data Integrity Cryptosuites](https://www.w3.org/TR/vc-data-integrity/)
    * [ECDSA-SD-2023](https://github.com/filip26/iron-ecdsa-sd-2023) [selective disclosure]
    * [EdDSA-RDFC-2022](https://github.com/filip26/iron-eddsa-rdfc-2022)
    * [ECDSA-RDFC-2019](https://github.com/filip26/iron-ecdsa-rdfc-2019) [P-256, P-384]
    * [BBS-2023](https://github.com/filip26/iron-bbs-cryptosuite-2023) (planned)
  * [Ed25519Signature2020](https://github.com/filip26/iron-ed25519-cryptosuite-2020)
  * Have you implemented a signature suite? List it here, open PR.
* Data Models
  * [v2.0](https://w3c.github.io/vc-data-model/)
  * [v1.1](https://www.w3.org/TR/vc-data-model/)

[Community compatibility dashboard for Verifiable Credentials](https://canivc.com/)
  
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

### Gradle
Android 12+ (API Level >=31)

```gradle
implementation("com.apicatalog:iron-verifiable-credentials-jre8:0.14.0")
```
Do you need to support an older Android version? [Contact me](mailto:filip26@gmail.com)

## Usage

This repository provides common logic and primitives to easily implement a signature suite. It is intended to be used together with a suite, or suites, of your choice, e.g. [ECDSA SD 2023](https://github.com/filip26/iron-ecdsa-sd-2023). Read the suite(s) documentation for specifics.

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

[![javadoc](https://javadoc.io/badge2/com.apicatalog/iron-verifiable-credentials/javadoc.svg)](https://javadoc.io/doc/com.apicatalog/iron-verifiable-credentials)

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

## Extensions
* [Iridium](https://github.com/filip26/iridium-cbor-ld) - A CBOR-based Processor for Linked Data
* [VC HTTP API & Service](https://github.com/filip26/iron-vc-api)
* [LEXREX](https://lexrex.web.app/) - Semantic vocabularies visual builder and manager

## Resources

* [VC Playground](https://vcplayground.org/)
* [Community compatibility dashboard for Verifiable Credentials](https://canivc.com/)
* [W3C Verifiable Credentials Data Model v2.0](https://w3c.github.io/vc-data-model/)
* [W3C Verifiable Credentials Data Model v1.1](https://www.w3.org/TR/vc-data-model/)
* [W3C Verifiable Credentials Use Cases](https://www.w3.org/TR/vc-use-cases/)
* [W3C Verifiable Credentials Implementation Guidelines 1.0](https://www.w3.org/TR/vc-imp-guide/)
* [W3C Decentralized Identifiers (DIDs) v1.0](https://www.w3.org/TR/did-core/)
* [The did:key Method v0.7](https://w3c-ccg.github.io/did-method-key/)

## Sponsors

<a href="https://github.com/digitalbazaar">
  <img src="https://avatars.githubusercontent.com/u/167436?s=200&v=4" width="40" />
</a> 

## Commercial Support
Commercial support is available at filip26@gmail.com
