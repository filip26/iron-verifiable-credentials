package com.apicatalog.di.suite;

import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.di.signature.ProofValue;
import com.apicatalog.di.signature.ProofValueGenerator;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.trust.model.DataModel;
import com.apicatalog.trust.processor.PayloadProcessor;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.signature.Signature;
import com.apicatalog.trust.signature.SignatureGenerator;

public final class MLDSA2024 extends StandardCryptoSuite {

    public static final String ALGORITHM_44 = "ML-DSA-44";
    public static final int SIGNATURE_LENGTH = 2420;
    public static final int PUBLIC_KEY_SIZE = 1312;

    private static MLDSA2024 MLDSA_44_RDFC_2024 = new MLDSA2024(
            "mldsa44-rdfc-2024",
            DataModel.C14N_RDFC,
            ProofValueGenerator::generateWithSHA256);

    private static MLDSA2024 MLDSA_44_JCS_2024 = new MLDSA2024(
            "mldsa44-jcs-2024",
            DataModel.C14N_JCS,
            ProofValueGenerator::generateWithSHA256);

    private MLDSA2024(
            String id, 
            String c14n,
            SignatureGenerator<DataIntegrityProof> signatureGenerator) {
        super(id, c14n, signatureGenerator);
    }

    public static MLDSA2024 get44(String c14n) {
        return switch (c14n) {
        case DataModel.C14N_RDFC -> MLDSA_44_RDFC_2024;
        case DataModel.C14N_JCS -> MLDSA_44_JCS_2024;
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
    public Signature decode(String encoded, Proof proof, PayloadProcessor payload) {
        return decode44(encoded, proof, payload);
    }

    public String encode(Signature signature) {
        return Multibase.BASE_64_URL.encode(signature.toByteArray());
    }

    private Signature decode44(String value, Proof proof, PayloadProcessor payload) {

        var signature = Multibase.BASE_64_URL.decode(value);

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
