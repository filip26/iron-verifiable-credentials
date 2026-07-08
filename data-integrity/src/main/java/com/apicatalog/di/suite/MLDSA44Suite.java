package com.apicatalog.di.suite;

import com.apicatalog.di.signature.ProofValue;
import com.apicatalog.di.signature.ProofValueGenerator;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.trust.data.Data;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.signature.Signature;

public class MLDSA44Suite {

    public static final String ALGORITHM = "ML-DSA-44";
    public static final int SIGNATURE_LENGTH = 2420;
    public static final int PUBLIC_KEY_SIZE = 1312;

    public static CryptoSuite newRDFC2024() {
        return new AtomicCryptoSuite(
                "mldsa44-rdfc-2024",
                "RDFC",
                Multibase.BASE_64_URL,
                MLDSA44Suite::decode,
                ProofValueGenerator::generateWithSHA256);
    }

    public static CryptoSuite newJCS2024() {
        return new AtomicCryptoSuite(
                "mldsa44-jcs-2024",
                "JCS",
                Multibase.BASE_64_URL,
                MLDSA44Suite::decode,
                ProofValueGenerator::generateWithSHA256);
    }
    
    static Signature decode(String value, Proof proof, Data data) {

        var signature = Multibase.BASE_64_URL.decode(value);

        if (signature.length != SIGNATURE_LENGTH) {
            throw new IllegalArgumentException(
                    """
                    ... invalid signature size ... %d bytes, expected %d bytes.
                    """.formatted(signature.length, SIGNATURE_LENGTH));
        }

        return ProofValue.newSignature(
                ALGORITHM,
                "SHA-256",
                signature,
                proof,
                data);
    }
}
