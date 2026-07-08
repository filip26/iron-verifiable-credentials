package com.apicatalog.di.suite;

import com.apicatalog.di.signature.ProofValueDecoder;
import com.apicatalog.di.signature.ProofValueGenerator;
import com.apicatalog.multibase.Multibase;

public class MLDSA44Suite {

    public static final String ALGORITHM = "ML-DSA-44";
    public static final int SIGNATURE_LENGTH = 2420;
    public static final int PUBLIC_KEY_SIZE = 1312;

    public static CryptoSuite newRDFC2024() {
        return new AtomicCryptoSuite(
                "mldsa44-rdfc-2024",
                "RDFC",
                Multibase.BASE_64_URL,
                new ProofValueDecoder(ALGORITHM, "SHA-256", Multibase.BASE_64_URL, SIGNATURE_LENGTH),
                new ProofValueGenerator("SHA-256"));
    }

    public static CryptoSuite newJCS2024() {
        return new AtomicCryptoSuite(
                "mldsa44-jcs-2024",
                "JCS",
                Multibase.BASE_64_URL,
                new ProofValueDecoder(ALGORITHM, "SHA-256", Multibase.BASE_64_URL, SIGNATURE_LENGTH),
                new ProofValueGenerator("SHA-256"));
    }
}
