package com.apicatalog.di.suite;

import java.security.SignatureException;
import java.util.function.Function;

import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.di.sd.SDBaseDocument;
import com.apicatalog.di.sd.SDBaseProofValue;
import com.apicatalog.di.sd.SDDerivedProofValue;
import com.apicatalog.di.sd.SDPayloadGenerator;
import com.apicatalog.di.sd.SDProofValue.SignatureAlgorithm;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multicodec.Multicodec;
import com.apicatalog.multicodec.codec.KeyCodec;
import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.security.Digestor;
import com.apicatalog.trust.model.Model;
import com.apicatalog.trust.payload.PayloadGenerator;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.signature.Signature;

public final class ECDSASD2023 implements CryptoSuite {

    public static final String ID = "ecdsa-sd-2023";

    public static final String P256 = "P-256";
    public static final String P384 = "P-384";

    private static final SignatureAlgorithm P256_ALGORITHMS = new SignatureAlgorithm(P256, Digestor.SHA_256);
    private static final SignatureAlgorithm P384_ALGORITHMS = new SignatureAlgorithm(P384, Digestor.SHA_384);

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

    public ProofDraft createProofDraft() throws SignatureException {
        return new ProofDraft(this);
    }

    @Override
    public Signature decode(String value, Proof proof, PayloadGenerator payload) {

        if (payload instanceof SDPayloadGenerator sdPayload) {

            var signature = Multibase.BASE_64_URL.decode(value);

            if (SDBaseProofValue.isAccepted(signature)) {
                return SDBaseProofValue.decode(
                        signature,
                        ECDSASD2023::getAlgorithm,
                        proofPublicKeyDecoder,
                        (DataIntegrityProof) proof,
                        sdPayload);
            }

            if (SDDerivedProofValue.isAccepted(signature)) {
                return SDDerivedProofValue.decode(
                        signature,
                        ECDSASD2023::getAlgorithm,
                        proofPublicKeyDecoder,
                        (DataIntegrityProof) proof,
                        sdPayload);
            }

            throw new IllegalArgumentException();

        }
        throw new IllegalStateException("Unsupported payload genetaror: " + payload);
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
        return Model.C14N_RDFC;
    }

    public static class ProofDraft extends DataIntegrityProof.Draft {

        final Function<byte[], Multicodec> proofPublicKeyDecoder;

        public ProofDraft(ECDSASD2023 cryptosuite) {
            super(cryptosuite);
            this.proofPublicKeyDecoder = cryptosuite.proofPublicKeyDecoder;
        }

        public DataIntegrityProof sign(
                String algorithm,
                AsymmetricSigner baseSigner,
                byte[] proofPublicKey,
                AsymmetricSigner proofSigner,
                Digestor.Factory digestFactory,
                SDBaseDocument payload) throws SignatureException {

            var digestAlgorithm = switch (algorithm) {
            case P256 -> Digestor.SHA_256;
            case P384 -> Digestor.SHA_384;
            default -> throw new IllegalArgumentException();
            };

            canonize(Model.C14N_RDFC);

            var unsignedProof = unsigned();

            var signature = SDBaseProofValue.generateSignature(
                    algorithm,
                    digestAlgorithm,
                    baseSigner,
                    proofPublicKey,
                    proofPublicKeyDecoder.apply(proofPublicKey),
                    proofSigner,
                    digestFactory.newDigestor(digestAlgorithm),
                    unsignedProof,
                    payload);

            return signed(signature);
        }
    }

    private static SignatureAlgorithm getAlgorithm(int signatureLength) {
        return switch (signatureLength) {
        case 64 -> P256_ALGORITHMS;
        case 96 -> P384_ALGORITHMS;
        default -> throw new IllegalArgumentException();
        };
    }
}