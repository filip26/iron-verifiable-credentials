package com.apicatalog.di.suite;

import com.apicatalog.di.signature.ProofValue;
import com.apicatalog.di.signature.ProofValueGenerator;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.trust.model.DataModel;
import com.apicatalog.trust.payload.PayloadSelector;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.signature.Signature;

public class SLHDSA2024 {

    public static final String ALGORITHM_SHA2_128s = "SLH-DSA-SHA2-128s";
    public static final int SIGNATURE_LENGTH = 7856;
    public static final int PUBLIC_KEY_SIZE = 32;
    public static final int PRIVATE_KEY_SIZE = 64;

    private static CryptoSuite SLHDSA_128s_RDFC_2024 = new CryptoSuite(
            "slhdsa128-rdfc-2024",
            DataModel.C14N_RDFC,
            Multibase.BASE_64_URL,
            SLHDSA2024::decode128s,
            ProofValueGenerator::generateWithSHA256);

    private static CryptoSuite SLHDSA_128s_JCS_2024 = new CryptoSuite(
            "slhdsa128-jcs-2024",
            DataModel.C14N_JCS,
            Multibase.BASE_64_URL,
            SLHDSA2024::decode128s,
            ProofValueGenerator::generateWithSHA256);

    public static CryptoSuite get128s(String c14n) {
        return switch (c14n) {
        case DataModel.C14N_RDFC -> SLHDSA_128s_RDFC_2024;
        case DataModel.C14N_JCS -> SLHDSA_128s_JCS_2024;
        default -> throw new IllegalArgumentException();
        };
    }

    public static CryptoSuite get128withRDFC() {
        return SLHDSA_128s_RDFC_2024;
    }

    public static CryptoSuite get128withJCS() {
        return SLHDSA_128s_JCS_2024;
    }

    private static Signature decode128s(String value, Proof proof, PayloadSelector payload) {

        var signature = Multibase.BASE_64_URL.decode(value);

        if (signature.length != SIGNATURE_LENGTH) {
            throw new IllegalArgumentException(
                    """
                    ... invalid signature size ... %d bytes, expected %d bytes.
                    """.formatted(signature.length, SIGNATURE_LENGTH));
        }

        return ProofValue.newInstance(
                ALGORITHM_SHA2_128s,
                "SHA-256",
                signature,
                proof,
                payload);
    }
}
