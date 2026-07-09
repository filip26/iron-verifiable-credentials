package com.apicatalog.di.suite;

import com.apicatalog.di.signature.ProofValue;
import com.apicatalog.di.signature.ProofValueGenerator;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.trust.data.Data;
import com.apicatalog.trust.model.DataModel;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.signature.Signature;

public class MLDSA2024 {

    public static final String ALGORITHM_44 = "ML-DSA-44";
    public static final int SIGNATURE_LENGTH = 2420;
    public static final int PUBLIC_KEY_SIZE = 1312;

    private static CryptoSuite MLDSA_44_RDFC_2024 = new CryptoSuite(
            "mldsa44-rdfc-2024",
            "RDFC",
            Multibase.BASE_64_URL,
            MLDSA2024::decode44,
            ProofValueGenerator::generateWithSHA256);

    private static CryptoSuite MLDSA_44_JCS_2024 = new CryptoSuite(
            "mldsa44-jcs-2024",
            "JCS",
            Multibase.BASE_64_URL,
            MLDSA2024::decode44,
            ProofValueGenerator::generateWithSHA256);

    public static CryptoSuite get44(String c14n) {
        return switch (c14n) {
        case DataModel.C14N_RDFC -> MLDSA_44_RDFC_2024;
        case DataModel.C14N_JCS -> MLDSA_44_JCS_2024;
        default -> throw new IllegalArgumentException();
        };
    }

    public static CryptoSuite get44withRDFC() {
        return MLDSA_44_RDFC_2024;
    }

    public static CryptoSuite get44withJCS() {
        return MLDSA_44_JCS_2024;
    }

    private static Signature decode44(String value, Proof proof, Data data) {

        var signature = Multibase.BASE_64_URL.decode(value);

        if (signature.length != SIGNATURE_LENGTH) {
            throw new IllegalArgumentException(
                    """
                    ... invalid signature size ... %d bytes, expected %d bytes.
                    """.formatted(signature.length, SIGNATURE_LENGTH));
        }

        return ProofValue.newSignature(
                ALGORITHM_44,
                "SHA-256",
                signature,
                proof,
                data);
    }
}
