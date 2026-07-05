
# Iron Verifiable Credentials

[![Java 25 CI](https://github.com/filip26/iron-verifiable-credentials/actions/workflows/build.yml/badge.svg)](https://github.com/filip26/iron-verifiable-credentials/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.apicatalog/iron-verifiable-credentials.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:com.apicatalog%20AND%20a:iron-verifiable-credentials)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

> [!IMPORTANT]
> v1.0.0 development is actively in progress. Breaking changes may occur prior to the stable release.

> [!NOTE]
> Your feedback is essential to the improvement of this library. Please share any concerns, primary use cases, areas for enhancement, or challenges you have encountered. Your insights help refine and optimize the library to better meet user needs. Thank you for your time and contributions.


## Supported Data Integrity Cryptosuites

The following cryptographic suites are supported out-of-the-box.

| Name | Algorithm | C14N | ⚛️ Quantum Resistant |
| :--- | :---: | :---: | :---: |
| ecdsa-rdfc-2019 | P-256 | RDFC | |
| ecdsa-rdfc-2019 | P-384 | RDFC | |
| ecdsa-jcs-2019 | P-256 | JCS | |
| ecdsa-jcs-2019 | P-384 | JCS | |
| eddsa-rdfc-2022 | Ed25519 | RDFC | |
| eddsa-jcs-2022 | Ed25519 | JCS | |
| Ed25519Signature2020 | Ed25519 | RDFC | |
| mldsa44-rdfc-2024 | ML-DSA-44 | RDFC | ✅ |
| mldsa44-jcs-2024 | ML-DSA-44 | JCS | ✅ |

## 🤝 Contributing

Contributions of all kinds are welcome - whether it’s code, documentation, testing, or community support! Please open PR or issue to get started.

## 📚 Resources

* [Verifiable Credential Data Integrity 1.1](https://www.w3.org/TR/vc-data-integrity-1.1/)
* [Data Integrity EdDSA Cryptosuites v1.1](https://www.w3.org/TR/vc-di-eddsa-1.1/)
* [Data Integrity ECDSA Cryptosuites v1.1](https://www.w3.org/TR/vc-di-ecdsa-1.1/)
* [Quantum-Resistant Cryptosuites v1.0](https://www.w3.org/TR/vc-di-quantum-resistant-1.0/)
* [Verifiable Credentials Data Model v2.0](https://w3c.github.io/vc-data-model/)
* [Verifiable Credentials Data Model v1.1](https://www.w3.org/TR/vc-data-model/)

## 💼 Commercial Support

Commercial support and consulting are available.
For inquiries, please contact: filip26@gmail.com
