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

## Verifying 

```java

try {
  Vc.verify(credentials|presentation)
  
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

## Issuing

```java
signed = Vc.sign(credentials, keys, proofOptions)

           // optional options
           .base(...)
           .loader(documentLoader) 
           .useBundledContexts(true|false)
           
           // returns signed document in expanded form
           .getExpanded(); 

signed = Vc.sign(credentials, keys, proofOptions)

           // returns signed document in compacted form
           .getCompacted(context);

```


# Building

Fork and clone the project repository.

## Java 17
```bash
> cd iron-verfiable-credentials
> mvn clean package
```

## Java 8
```bash
> cd iron-verfiable-credentials
> mvn -f pom_jre8.xml clean package
```

# Resources
* [Verifiable Credentials Data Model v1.1](https://www.w3.org/TR/vc-data-model/)
* [Verifiable Credentials Use Cases](https://www.w3.org/TR/vc-use-cases/)
* [Verifiable Credentials Implementation Guidelines 1.0](https://www.w3.org/TR/vc-imp-guide/)
* [Data Integrity 1.0](https://w3c-ccg.github.io/data-integrity-spec/)
* [Ed25519 Signature 2020](https://w3c-ccg.github.io/lds-ed25519-2020/)
* [The did:key Method v0.7](https://w3c-ccg.github.io/did-method-key/)
* [Decentralized Identifiers (DIDs) v1.0](https://www.w3.org/TR/did-core/)


