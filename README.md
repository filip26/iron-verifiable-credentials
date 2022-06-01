# Iron Verifiable Credentials Processor & API

An implementation of the [Verifiable Credentials](https://www.w3.org/TR/vc-data-model/) in Java.

**under active development, use at your own risk, everything is a subject to change**

[![Java 17 Build](https://github.com/filip26/iron-verifiable-credentials/actions/workflows/java17-build.yml/badge.svg)](https://github.com/filip26/iron-verifiable-credentials/actions/workflows/java17-build.yml)
[![Android (JDK8) Build](https://github.com/filip26/iron-verifiable-credentials/actions/workflows/android-build.yml/badge.svg)](https://github.com/filip26/iron-verifiable-credentials/actions/workflows/android-build.yml)

# Contributing

All PR's welcome!

# Usage

```java

boolean valid = Vc.verify(credentials).isValid();

signedCredentials = Vc.sign(credentials, keys, proofOptions)
                      .loader(documentLoader) // custom loader
                      .get();   // returns signed document in expanded form

signedCredentials = Vc.sign(credentials, keys, proofOptions)
                      .getCompacted(context); // returns signed document in compacted form

KeyPair keys = Vc.newKeys("https://w3id.org/security#Ed25519KeyPair2020", 32)
                 .generate()

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


