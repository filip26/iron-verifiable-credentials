package com.apicatalog.di.suite;

import com.apicatalog.di.signature.ProofValue;
import com.apicatalog.di.signature.ProofValueGenerator;
import com.apicatalog.di.std.StandardCryptoSuite;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.trust.model.ProcessingModel;
import com.apicatalog.trust.processor.PayloadProcessor;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.signature.Signature;

public final class SLHDSA2024 extends StandardCryptoSuite {

    public static final String ALGORITHM_SHA2_128s = "SLH-DSA-SHA2-128s";
    public static final int SIGNATURE_LENGTH = 7856;
    public static final int PUBLIC_KEY_SIZE = 32;
    public static final int PRIVATE_KEY_SIZE = 64;

    private static SLHDSA2024 SLHDSA_128s_RDFC_2024 = new SLHDSA2024(
            "slhdsa128-rdfc-2024",
            ProcessingModel.C14N_RDFC);

    private static SLHDSA2024 SLHDSA_128s_JCS_2024 = new SLHDSA2024(
            "slhdsa128-jcs-2024",
            ProcessingModel.C14N_JCS);

    private SLHDSA2024(
            String id,
            String c14n) {
        super(id, c14n, Multibase.BASE_64_URL, ProofValueGenerator::generateWithSHA256);
    }

    public static CryptoSuite get128sInstance(String c14n) {
        return switch (c14n) {
        case ProcessingModel.C14N_RDFC -> SLHDSA_128s_RDFC_2024;
        case ProcessingModel.C14N_JCS -> SLHDSA_128s_JCS_2024;
        default -> throw new IllegalArgumentException();
        };
    }

    public static SLHDSA2024 get128withRDFC() {
        return SLHDSA_128s_RDFC_2024;
    }

    public static SLHDSA2024 get128withJCS() {
        return SLHDSA_128s_JCS_2024;
    }

    protected Signature decode(byte[] signature, Proof proof, PayloadProcessor payload) {
        return decode128s(signature, proof, payload);
    }

    private static Signature decode128s(byte[] signature, Proof proof, PayloadProcessor payload) {
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
