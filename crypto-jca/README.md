# Iron Data Integrity: Java Cryptography Architecture (JCA)

[![Maven Central](https://img.shields.io/maven-central/v/com.apicatalog/iron-crypto-jca.svg?label=Central)](https://mvnrepository.com/artifact/com.apicatalog/iron-crypto-jca)
[![javadoc](https://javadoc.io/badge2/com.apicatalog/iron-crypto-jca/javadoc.svg)](https://javadoc.io/doc/com.apicatalog/iron-crypto-jca)

An asymmetric cryptography implementation based on the Java Cryptography Architecture (JCA) for W3C Data Integrity specifications.

| Algorithm | Sign | Verify | ⚛️ Quantum Resistant |
| :--- | :--- | :--- | :--- |
| P-256 | ✅ | ✅ | ❌ |
| P-384 | ✅ | ✅ | ❌ |
| Ed25519 | ✅ | ✅ | ❌ |
| ML-DSA-44 | ✅ | ✅ | ✅ |

Provides signing and verification algorithms for W3C Data Integrity cryptosuites:

|  Algorithm   |  Cryptosuites    |
| :- | :-   | 
| P-256, P-384 | `ecdsa-rdfc-2019`, `ecdsa-jcs-2019`, `ecdsa-sd-2023` | 
| Ed25519 |  `eddsa-rdfc-2022`, `eddsa-jcs-2022`, `Ed25519Signature2020` |
| ML-DSA-44  | `mldsa44-rdfc-2024`, `mldsa44-jcs-2024` |

## 📦 Installation

```xml
<dependency>
    <groupId>com.apicatalog</groupId>
    <artifactId>iron-crypto-jca</artifactId>
    <version>${crypto-jca.version}</version>
</dependency>
```

