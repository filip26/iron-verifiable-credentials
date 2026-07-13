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

public final class SLHDSA2024 extends StandardCryptoSuite {

    public static final String ALGORITHM_SHA2_128s = "SLH-DSA-SHA2-128s";
    public static final int SIGNATURE_LENGTH = 7856;
    public static final int PUBLIC_KEY_SIZE = 32;
    public static final int PRIVATE_KEY_SIZE = 64;

    private static SLHDSA2024 SLHDSA_128s_RDFC_2024 = new SLHDSA2024(
            "slhdsa128-rdfc-2024",
            DataModel.C14N_RDFC,
            ProofValueGenerator::generateWithSHA256);

    private static SLHDSA2024 SLHDSA_128s_JCS_2024 = new SLHDSA2024(
            "slhdsa128-jcs-2024",
            DataModel.C14N_JCS,
            ProofValueGenerator::generateWithSHA256);

    private SLHDSA2024(
            String id,
            String c14n,
            SignatureGenerator<DataIntegrityProof> signatureGenerator) {
        super(id, c14n, signatureGenerator);
    }

    public static CryptoSuite get128s(String c14n) {
        return switch (c14n) {
        case DataModel.C14N_RDFC -> SLHDSA_128s_RDFC_2024;
        case DataModel.C14N_JCS -> SLHDSA_128s_JCS_2024;
        default -> throw new IllegalArgumentException();
        };
    }

    public static SLHDSA2024 get128withRDFC() {
        return SLHDSA_128s_RDFC_2024;
    }

    public static SLHDSA2024 get128withJCS() {
        return SLHDSA_128s_JCS_2024;
    }

    @Override
    public Signature decode(String encoded, Proof proof, PayloadProcessor payload) {
        return decode128s(encoded, proof, payload);
    }

    public String encode(Signature signature) {
        return Multibase.BASE_64_URL.encode(signature.toByteArray());
    }

    private static Signature decode128s(String value, Proof proof, PayloadProcessor payload) {

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
