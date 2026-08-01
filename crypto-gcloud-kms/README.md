# Iron Data Integrity: Google Cloud KMS-based Crypto

An asymmetric cryptography implementation based on the Google Cloud KMS for W3C Data Integrity specifications.

| Algorithm | Sign | Verify | ⚛️ Quantum Resistant |
| :--- | :--- | :--- | :--- |
| P-256 | ✅ | ❌ | ❌ |
| P-384 | ✅ | ❌ | ❌ |
| Ed25519 | ✅ | ❌ | ❌ |
| ML-DSA-44 | ✅ | ❌ | ✅ |
| ML-DSA-87 | ✅ | ❌ | ✅ |
| SLH-DSA-SHA2-128s | ✅ | ❌ | ✅ |

Provides signing algorithms for W3C Data Integrity cryptosuites:

|  Algorithm   |  Cryptosuites    |
| :- | :-   | 
| P-256, P-384 | `ecdsa-rdfc-2019`, `ecdsa-jcs-2019`, `ecdsa-sd-2023` | 
| Ed25519 |  `eddsa-rdfc-2022`, `eddsa-jcs-2022`, `Ed25519Signature2020` |
| ML-DSA-44  | `mldsa44-rdfc-2024`, `mldsa44-jcs-2024` |
| SLH-DSA-SHA2-128s | `slhdsa128-rdfc-2024`, `slhdsa128-jcs-2024` |

## 📦 Installation

```xml
<dependency>
    <groupId>com.apicatalog</groupId>
    <artifactId>iron-crypto-gcloud-kms</artifactId>
    <version>${crypto-gckms.version}</version>
</dependency>
```

