# Iron Data Integrity: Bouncy Castle Crypto

[![Maven Central](https://img.shields.io/maven-central/v/com.apicatalog/iron-crypto-bc.svg?label=Central)](https://mvnrepository.com/artifact/com.apicatalog/iron-crypto-bc)
[![javadoc](https://javadoc.io/badge2/com.apicatalog/iron-crypto-bc/javadoc.svg)](https://javadoc.io/doc/com.apicatalog/iron-crypto-bc)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Bouncy Castle based asymmetric cryptography implementation for W3C Data Integrity specifications.

| Algorithm | Sign | Verify |⚛️ Quantum Resistant |
| :--- | :--- | :--- | :--- |
| P-256 | ✅ | ✅ | ❌ |
| P-384 | ✅ | ✅ | ❌ |
| Ed25519 | ✅ | ✅ | ❌ |
| ML-DSA-44 | ✅ | ✅ | ✅ |
| SLH-DSA-SHA2-128s | ✅ | ✅ | ✅ |

Provides signing and verification algorithms for W3C Data Integrity cryptosuites:

|  Algorithm   |  CryptoSuites    |
| :- | :-   | 
| P-256, P-384 | `ecdsa-rdfc-2019`, `ecdsa-jcs-2019`, `ecdsa-sd-2023` | 
| Ed25519 |  `eddsa-rdfc-2022`, `eddsa-jcs-2022`, `Ed25519Signature2020` |
| ML-DSA-44  | `mldsa44-rdfc-2024`, `mldsa44-jcs-2024` |
| SLH-DSA-SHA2-128s | `slhdsa128-rdfc-2024`, `slhdsa128-jcs-2024` |

## 📦 Installation

```xml
<dependency>
    <groupId>com.apicatalog</groupId>
    <artifactId>iron-crypto-bc</artifactId>
    <version>${crypto-bc.version}</version>
</dependency>
```

