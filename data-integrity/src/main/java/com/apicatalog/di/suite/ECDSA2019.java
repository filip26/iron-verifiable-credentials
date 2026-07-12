package com.apicatalog.di.suite;

import java.security.MessageDigest;
import java.security.SignatureException;
import java.util.function.Function;

import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.di.signature.ProofValue;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.trust.model.DataModel;
import com.apicatalog.trust.payload.DigestiblePayload;
import com.apicatalog.trust.processor.PayloadSelector;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.signature.Signature;

public class ECDSA2019 {

    public static final String P256 = "P-256";
    public static final String P384 = "P-384";

    private static final CryptoSuite ECDSA_RDFC_2019 = new CryptoSuite(
            "ecdsa-rdfc-2019",
            DataModel.C14N_RDFC,
            Multibase.BASE_58_BTC,
            ECDSA2019::decode,
            ECDSA2019::generate);

    private static final CryptoSuite ECDSA_JCS_2019 = new CryptoSuite(
            "ecdsa-jcs-2019",
            DataModel.C14N_JCS,
            Multibase.BASE_58_BTC,
            ECDSA2019::decode,
            ECDSA2019::generate);

    public static CryptoSuite withRDFC() {
        return ECDSA_RDFC_2019;
    }

    public static CryptoSuite withJCS() {
        return ECDSA_JCS_2019;
    }

    private static Signature decode(String value, Proof proof, PayloadSelector payload) {

        var signature = Multibase.BASE_58_BTC.decode(value);

        String algorithm = null;
        String digest = null;

        switch (signature.length) {
        case 64:
            algorithm = P256;
            digest = "SHA-256";
            break;
        case 96:
            algorithm = P384;
            digest = "SHA-384";
            break;
        default:
            throw new IllegalArgumentException();
        }

        return ProofValue.newInstance(
                algorithm,
                digest,
                signature,
                proof,
                payload);
    }

    private static Signature generate(
            String algorithm,
            AsymmetricSigner signer,
            Function<String, MessageDigest> digestFactory,
            DataIntegrityProof proof,
            DigestiblePayload payload)
            throws SignatureException {

        var digestor = switch (algorithm) {
        case P256 -> digestFactory.apply("SHA-256");
        case P384 -> digestFactory.apply("SHA-384");
        default -> throw new IllegalArgumentException();
        };

        return ProofValue.generateSignature(
                algorithm,
                signer,
                digestor,
                proof,
                payload);
    }
}
