package com.apicatalog.di.signature;

import com.apicatalog.multibase.Multibase;
import com.apicatalog.trust.data.Data;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.signature.Signature;
import com.apicatalog.trust.signature.SignatureDecoder;

public class ProofValueDecoder implements SignatureDecoder {

    String algorithm; // P-256, P-384, Ed25519, ML-DSA-44, ...
    String digestAlgorithm;
    Multibase multibase;
    int length;

    public ProofValueDecoder(
            String algorithm,
            String digestAlgorithm,
            Multibase multibase,
            int length) {
        this.algorithm = algorithm;
        this.digestAlgorithm = digestAlgorithm;
        this.multibase = multibase;
        this.length = length;
    }

    @Override
    public Signature decode(String value, Proof proof, Data data) {

        var signature = multibase.decode(value);

        if (signature.length != length) {
            throw new IllegalArgumentException(
                    """
                    ... invalid signature size ... %d bytes, expected %d bytes.
                    """.formatted(signature.length, length));
        }

        return ProofValue.newSignature(
                algorithm,
                digestAlgorithm,
                signature,
                proof,
                data);
    }
}
