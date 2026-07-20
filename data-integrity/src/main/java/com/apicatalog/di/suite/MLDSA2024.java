package com.apicatalog.di.suite;

import com.apicatalog.di.signature.ProofValue;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.trust.model.ProcessingModel;
import com.apicatalog.trust.payload.PayloadGenerator;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.signature.Signature;

public final class MLDSA2024 extends StandardCryptoSuite {

    public static final String ALGORITHM_44 = "ML-DSA-44";
    public static final int SIGNATURE_LENGTH = 2420;
    public static final int PUBLIC_KEY_SIZE = 1312;

    private static MLDSA2024 MLDSA_44_RDFC_2024 = new MLDSA2024(
            "mldsa44-rdfc-2024",
            ProcessingModel.C14N_RDFC);

    private static MLDSA2024 MLDSA_44_JCS_2024 = new MLDSA2024(
            "mldsa44-jcs-2024",
            ProcessingModel.C14N_JCS);

    private MLDSA2024(
            String id,
            String c14n) {
        super(id, c14n, Multibase.BASE_64_URL, ProofValue::generateSignatureWithSHA256);
    }

    public static MLDSA2024 get44Instance(String c14n) {
        return switch (c14n) {
        case ProcessingModel.C14N_RDFC -> MLDSA_44_RDFC_2024;
        case ProcessingModel.C14N_JCS -> MLDSA_44_JCS_2024;
        default -> throw new IllegalArgumentException();
        };
    }

    public static MLDSA2024 get44withRDFC() {
        return MLDSA_44_RDFC_2024;
    }

    public static MLDSA2024 get44withJCS() {
        return MLDSA_44_JCS_2024;
    }

    @Override
    public Signature decode(byte[] signature, Proof proof, PayloadGenerator payload) {
        return decode44(signature, proof, payload);
    }

    private Signature decode44(byte[] signature, Proof proof, PayloadGenerator payload) {
        if (signature.length != SIGNATURE_LENGTH) {
            throw new IllegalArgumentException(
                    """
                    ... invalid signature size ... %d bytes, expected %d bytes.
                    """.formatted(signature.length, SIGNATURE_LENGTH));
        }

        return ProofValue.newInstance(
                ALGORITHM_44,
                "SHA-256",
                signature,
                proof,
                payload);
    }
}
