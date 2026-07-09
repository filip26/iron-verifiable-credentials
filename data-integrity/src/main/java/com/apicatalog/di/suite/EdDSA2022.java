package com.apicatalog.di.suite;

import com.apicatalog.di.signature.ProofValue;
import com.apicatalog.di.signature.ProofValueGenerator;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.trust.data.Data;
import com.apicatalog.trust.model.DataModel;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.signature.Signature;

public class EdDSA2022 {

    public static final String ALGORITHM = "Ed25519";
    public static final int SIGNATURE_LENGTH = 64;

    private static final CryptoSuite MLDSA_44_RDFC_2024 = new AtomicCryptoSuite(
            "eddsa-rdfc-2022",
            "RDFC",
            Multibase.BASE_58_BTC,
            EdDSA2022::decode,
            ProofValueGenerator::generateWithSHA256);

    private static final CryptoSuite MLDSA_44_JCS_2024 = new AtomicCryptoSuite(
            "eddsa-jcs-2022",
            "JCS",
            Multibase.BASE_58_BTC,
            EdDSA2022::decode,
            ProofValueGenerator::generateWithSHA256);

    public static CryptoSuite withRDFC() {
        return MLDSA_44_RDFC_2024;
    }

    public static CryptoSuite withJCS() {
        return MLDSA_44_JCS_2024;
    }

    public static CryptoSuite get(String c14n) {
        return switch (c14n) {
        case DataModel.C14N_RDFC -> MLDSA_44_RDFC_2024;
        case DataModel.C14N_JCS -> MLDSA_44_JCS_2024;
        default -> throw new IllegalArgumentException();
        };
    }

    static Signature decode(String value, Proof proof, Data data) {

        var signature = Multibase.BASE_58_BTC.decode(value);

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
