# Iron Security Contexts

[![Maven Central](https://img.shields.io/maven-central/v/com.apicatalog/iron-security-contexts.svg?label=Central)](https://mvnrepository.com/artifact/com.apicatalog/iron-security-contexts)
[![javadoc](https://javadoc.io/badge2/com.apicatalog/iron-security-contexts/javadoc.svg)](https://javadoc.io/doc/com.apicatalog/security-contexts)

Static JSON-LD security contexts for W3C Verifiable Credentials Data Integrity specifications, providing offline resolution and integrity verification.

| Context | SHA2-256 Digest |
|---|---|
| `https://www.w3.org/ns/credentials/v2` | `59955ced6697d61e03f2b2556febe5308ab16842846f5b586d7f1f7adec92734` |
| `https://w3id.org/security/data-integrity/v2` | `67f21e6e33a6c14e5ccfd2fc7865f7474fb71a04af7e94136cb399dfac8ae8f4` |
| `https://www.w3.org/ns/did/v1` | `4f3eae5568c9c5f036a082088f9e192019ee06faa78973c87ff91d5421b88dad` |
| `https://w3id.org/security/suites/ed25519-2020/v1` | `b9e1ab971fd8bf2c7553e0c4a9438e0b9450afde1ea1ca5b2492368b9f549588` |
| `https://w3id.org/security/multikey/v1` | `ba2c182de2d92f7e47184bcca8fcf0beaee6d3986c527bf664c195bbc7c58597` |
| `https://w3id.org/security/jwk/v1` | `0f14b62f6071aafe00df265770ea0c7508e118247d79b7d861a406d2aa00bece` |
| `https://www.w3.org/ns/cid/v1` | `ea216ecc1cb02cd39b693dba2250141e270ba0bf95890be107dd9a9e8e43de85` |
| `https://www.w3.org/2018/credentials/v1` | `ab4ddd9a531758807a79a5b450510d61ae8d147eab966cc9a200c07095b0cdcc` |
| `https://w3id.org/security/data-integrity/v1` | `b5d829bd09aa7c42abc6efa0c8ed7635313b5487f37ccfce3ecd149ca9418554` |


## Examples

```javascript
// Obtain a context resource by URI
var resource = SecurityContexts.getContext("https://www.w3.org/ns/credentials/v2");

// Verify resource content against its SHA-256 digest
if (resource != null && resource.isValid()) {
     // Resource integrity confirmed
}
 
// Read context as an InputStream
try (var is = SecurityContexts.getContextAsStream("https://www.w3.org/ns/credentials/v2")) {
    // Read or parse JSON-LD
}

// Read context directly as a byte array
byte[] bytes = SecurityContexts.getContextAsBytes("https://www.w3.org/ns/credentials/v2");
 ```

## 📦 Installation

```xml
<dependency>
    <groupId>com.apicatalog</groupId>
    <artifactId>iron-security-contexts</artifactId>
    <version>${contexts.version}</version>
</dependency>
```

## 📚 Resources

- [Verifiable Credentials Vocabulary v2.0](https://www.w3.org/2018/credentials//)
- [Security Vocabulary](https://w3id.org/security)
