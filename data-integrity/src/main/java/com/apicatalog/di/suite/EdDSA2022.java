package com.apicatalog.di.suite;

import com.apicatalog.di.signature.ProofValue;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.trust.model.ProcessingModel;
import com.apicatalog.trust.payload.PayloadGenerator;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.signature.Signature;

public final class EdDSA2022 extends StandardCryptoSuite {

    public static final String ALGORITHM = "Ed25519";
    public static final int SIGNATURE_LENGTH = 64;

    private static final EdDSA2022 MLDSA_44_RDFC_2024 = new EdDSA2022(
            "eddsa-rdfc-2022",
            ProcessingModel.C14N_RDFC);

    private static final EdDSA2022 MLDSA_44_JCS_2024 = new EdDSA2022(
            "eddsa-jcs-2022",
            ProcessingModel.C14N_JCS);

    private EdDSA2022(String id, String c14n) {
        super(id, c14n, Multibase.BASE_58_BTC, ProofValue::generateSignatureWithSHA256);
    }

    public static EdDSA2022 withRDFC() {
        return MLDSA_44_RDFC_2024;
    }

    public static EdDSA2022 withJCS() {
        return MLDSA_44_JCS_2024;
    }

    public static CryptoSuite getInstance(String c14n) {
        return switch (c14n) {
        case ProcessingModel.C14N_RDFC -> MLDSA_44_RDFC_2024;
        case ProcessingModel.C14N_JCS -> MLDSA_44_JCS_2024;
        default -> throw new IllegalArgumentException();
        };
    }

    @Override
    public Signature decode(byte[] signature, Proof proof, PayloadGenerator payload) {
        if (signature.length != SIGNATURE_LENGTH) {
            throw new IllegalArgumentException(
                    """
                    ... invalid signature size ... %d bytes, expected %d bytes.
                    """.formatted(signature.length, SIGNATURE_LENGTH));
        }

        return ProofValue.newInstance(
                ALGORITHM,
                "SHA-256",
                signature,
                proof,
                payload);
    }
}
