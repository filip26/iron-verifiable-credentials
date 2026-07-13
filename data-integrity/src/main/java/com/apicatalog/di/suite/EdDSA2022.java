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

public final class EdDSA2022 extends StandardCryptoSuite {

    public static final String ALGORITHM = "Ed25519";
    public static final int SIGNATURE_LENGTH = 64;

    private static final EdDSA2022 MLDSA_44_RDFC_2024 = new EdDSA2022(
            "eddsa-rdfc-2022",
            DataModel.C14N_RDFC,
            ProofValueGenerator::generateWithSHA256);

    private static final EdDSA2022 MLDSA_44_JCS_2024 = new EdDSA2022(
            "eddsa-jcs-2022",
            DataModel.C14N_JCS,
            ProofValueGenerator::generateWithSHA256);

    private EdDSA2022(String id, String c14n, SignatureGenerator<DataIntegrityProof> signatureGenerator) {
        super(id, c14n, signatureGenerator);
    }

    public static EdDSA2022 withRDFC() {
        return MLDSA_44_RDFC_2024;
    }

    public static EdDSA2022 withJCS() {
        return MLDSA_44_JCS_2024;
    }

    public static CryptoSuite get(String c14n) {
        return switch (c14n) {
        case DataModel.C14N_RDFC -> MLDSA_44_RDFC_2024;
        case DataModel.C14N_JCS -> MLDSA_44_JCS_2024;
        default -> throw new IllegalArgumentException();
        };
    }

    public String encode(Signature signature) {
        return Multibase.BASE_58_BTC.encode(signature.toByteArray());
    }

    @Override
    public Signature decode(String encoded, Proof proof, PayloadProcessor payload) {

        var signature = Multibase.BASE_58_BTC.decode(encoded);

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
