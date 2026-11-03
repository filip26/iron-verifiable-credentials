> [!NOTE]
> 🚧 **Work in Progress** - Feedback of any kind is welcome and contributions are highly appreciated! Feel free to open an issue or submit a pull request.

# SD JSON Claims

Privacy-preserving selective disclosure of JSON documents using [Salted Merkle Tree](https://github.com/filip26/salted-merkle-tree). 

A JSON document is transformed into an ordered sequence of claims that are signed by an issuer. The signature tethered with the origin document is passed to a holder, which is a disclosing party having the ability to select a particular set of claims to be disclosed and shared as a JSON document with a verifier. The verifier does not receive all the claims included in the origin document, only the disclosed claims, and can verify the disclosed document fragment is the one signed by the issuer while undisclosed claims stay secret.

Selective disclosure is used in a three-party trust model allowing a verifier to validate a trusted issuer signature without interacting with the issuer, ensuring unlinkability and untraceability.

## Three-party trust model

1. Issuer: Generates cryptographic commitment to data, ensuring authenticity and integrity through digital signing.
2. Holder: Manages selective disclosure, choosing specific claims to reveal while preserving the immutable issuer signature.
3. Verifier: Validates the authenticity and integrity of the disclosed subset against the trusted issuer signature without requiring interaction with the issuer.

## Interoperability

* [SD JSON Claims.js](https://github.com/filip26/sd-json-claims.js)
