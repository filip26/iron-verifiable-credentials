# Iron Verifiable Credentials Processor & API

An implementation of the [Verifiable Credentials](https://www.w3.org/TR/vc-data-model/) in Java.

**under active development, use at your own risk, everything is a subject to change**

[![Java 17 CI](https://github.com/filip26/iron-verifiable-credentials/actions/workflows/java17-build.yml/badge.svg)](https://github.com/filip26/iron-verifiable-credentials/actions/workflows/java17-build.yml)
[![Android (Java 8) CI](https://github.com/filip26/iron-verifiable-credentials/actions/workflows/java8-build.yml/badge.svg)](https://github.com/filip26/iron-verifiable-credentials/actions/workflows/java8-build.yml)


## Features

* Verifying VC/VP
* Issuing VC/VP
* Signature
  * Ed25519 Signature 2020
  

# Contributing

All PR's welcome!

# Usage

```java

try {
  Vc.verify(credentials)
    .statusVerifier(...)    // optional
    .didResolver(...)       // optional  
    .isValid();
} catch (VerificationError | DataError e) {
  ...
}

signed = Vc.sign(credentials, keys, proofOptions)
           .loader(documentLoader) // optional custom loader
           .getExpanded(); // returns signed document in expanded form

signed = Vc.sign(credentials, keys, proofOptions)
           .getCompacted(context); // returns signed document in compacted form

```

# Building

Fork and clone the project repository.

```bash
> cd iron-verfiable-credentials
> mvn clean package
```

# Resources
* [Verifiable Credentials Data Model v1.1](https://www.w3.org/TR/vc-data-model/)
* [Verifiable Credentials Use Cases](https://www.w3.org/TR/vc-use-cases/)
* [Verifiable Credentials Implementation Guidelines 1.0](https://www.w3.org/TR/vc-imp-guide/)
* [Data Integrity 1.0](https://w3c-ccg.github.io/data-integrity-spec/)
* [Ed25519 Signature 2020](https://w3c-ccg.github.io/lds-ed25519-2020/)
* [The did:key Method v0.7](https://w3c-ccg.github.io/did-method-key/)
* [Decentralized Identifiers (DIDs) v1.0](https://www.w3.org/TR/did-core/)


