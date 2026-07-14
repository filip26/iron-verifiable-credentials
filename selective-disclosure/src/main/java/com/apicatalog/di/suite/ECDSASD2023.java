package com.apicatalog.di.suite;

import java.security.MessageDigest;
import java.security.SignatureException;
import java.util.function.Function;

import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.di.proof.DataIntegrityProof.Builder;
import com.apicatalog.di.sd.SDBaseProofValue;
import com.apicatalog.di.sd.SDDerivedProofValue;
import com.apicatalog.di.sd.SDGraphProcessor.SignatureAlgorithm;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multicodec.Multicodec;
import com.apicatalog.multicodec.codec.KeyCodec;
import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.trust.model.DataModel;
import com.apicatalog.trust.payload.RedactablePayload;
import com.apicatalog.trust.processor.PayloadProcessor;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.signature.Signature;

public final class ECDSASD2023 implements CryptoSuite {

    public static final String ID = "ecdsa-sd-2023";

    public static final String P256 = "P-256";
    public static final String P384 = "P-384";

    private static final SignatureAlgorithm P256_ALGORITHM = new SignatureAlgorithm(P256, "SHA-256");
    private static final SignatureAlgorithm P384_ALGORITHM = new SignatureAlgorithm(P384, "SHA-384");

    private static final ECDSASD2023 INSTANCE = new ECDSASD2023();

    private final Function<byte[], Multicodec> proofPublicKeyDecoder;

    private ECDSASD2023() {
        this(key -> {

            if (KeyCodec.P256_PUBLIC.isEncoded(key)) {
                return KeyCodec.P256_PUBLIC;
            }
            if (KeyCodec.P384_PUBLIC.isEncoded(key)) {
                return KeyCodec.P384_PUBLIC;
            }

            throw new IllegalArgumentException();
        });
    }

    private ECDSASD2023(Function<byte[], Multicodec> proofPublicKeyDecoder) {
        this.proofPublicKeyDecoder = proofPublicKeyDecoder;
    }

    public static ECDSASD2023 getInstance() {
        return INSTANCE;
    }

    public static ECDSASD2023 newInstance(Function<byte[], Multicodec> proofPublicKeyDecoder) {
        return INSTANCE;
    }

    public DataIntegrityProof sign(
            String algorithm,
            AsymmetricSigner baseSigner,
            byte[] proofPublicKey,
            AsymmetricSigner proofSigner,
            Function<String, MessageDigest> digestFactory,
            Builder proofDraft,
            RedactablePayload payload) throws SignatureException {

        proofDraft.canonize(DataModel.C14N_RDFC);

        var unsignedProof = proofDraft.snapshot();

        var digestor = switch (algorithm) {
        case P256 -> digestFactory.apply("SHA-256");
        case P384 -> digestFactory.apply("SHA-384");
        default -> throw new IllegalArgumentException();
        };

        var signature = SDBaseProofValue.generateSignature(
                algorithm,
                baseSigner,
                proofPublicKey,
                proofPublicKeyDecoder.apply(proofPublicKey),
                proofSigner,
                digestor,
                unsignedProof,
                payload);

        proofDraft.build(signature);
        return proofDraft.snapshot();

    }

    @Override
    public Signature decode(String value, Proof proof, PayloadProcessor data) {

        var signature = Multibase.BASE_64_URL.decode(value);
        IO.println(proofPublicKeyDecoder);
        if (SDBaseProofValue.isAccepted(signature)) {
            return SDBaseProofValue.decode(
                    signature,
                    ECDSASD2023::getAlgorithm,
                    proofPublicKeyDecoder,
                    proof,
                    data);
        }
        if (SDDerivedProofValue.isAccepted(signature)) {
            return SDDerivedProofValue.decode(
                    signature,
                    ECDSASD2023::getAlgorithm,
                    proofPublicKeyDecoder,
                    proof,
                    data);
        }

        throw new IllegalArgumentException();
    }

    @Override
    public String encode(Signature signature) {
        return Multibase.BASE_64_URL.encode(signature.toByteArray());
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String c14n() {
        return DataModel.C14N_RDFC;
    }

    static SignatureAlgorithm getAlgorithm(int signatureLength) {
        return switch (signatureLength) {
        case 64 -> P256_ALGORITHM;
        case 96 -> P384_ALGORITHM;
        default -> throw new IllegalArgumentException();
        };
    }
}