package com.apicatalog.di.sd;

import java.security.MessageDigest;
import java.security.SignatureException;
import java.util.function.Function;

import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.di.signature.ProofValue;
import com.apicatalog.di.suite.CryptoSuite;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.trust.data.Data;
import com.apicatalog.trust.model.DataModel;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.signature.Signature;

public final class ECDSASD2023 {

    public static final String CRYPTOSUITE_NAME = "ecdsa-sd-2023";

    public static final String P256 = "P-256";
    public static final String P384 = "P-384";

    public static CryptoSuite getInstance() {
        return new CryptoSuite(
                CRYPTOSUITE_NAME,
                DataModel.C14N_RDFC,
                Multibase.BASE_64_URL,
                ECDSASD2023::decode,
                ECDSASD2023::generate);
    }

    private static Signature decode(String value, Proof proof, Data data) {

        var signature = Multibase.BASE_64_URL.decode(value);
        IO.println("SD sig length: " + signature.length);
        String signatureAlgorithm = null;
        String digestAlgorithm = null;

//TODO
//        switch (signature.length) {
//        case 64:
        signatureAlgorithm = P256;
        digestAlgorithm = "SHA-256";
//            break;
//        case 96:
//            algorithm = P384;
//            digest = "SHA-384";
//            break;
//        default:
//            throw new IllegalArgumentException();
//        }

        if (BaseProofValue.isAccepted(signature)) {
            return BaseProofValue.decode(
                    signatureAlgorithm,
                    digestAlgorithm,
                    signature,
                    proof,
                    data);
        }

        throw new IllegalArgumentException();
    }

    private static Signature generate(
            String algorithm,
            AsymmetricSigner signer,
            Function<String, MessageDigest> digestFactory,
            DataIntegrityProof proof,
            Data data)
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
                data);
    }
//    static final CryptoSuite CRYPTO_256 = new CryptoSuite(
//            CRYPTOSUITE_NAME,
//            256,
//            new RDFC(),
//            new MessageDigest("SHA-256"),
//            new BCECDSASignatureProvider(CurveType.P256));
//
//    static final CryptoSuite CRYPTO_384 = new CryptoSuite(
//            CRYPTOSUITE_NAME,
//            384,
//            new RDFC(),
//            new MessageDigest("SHA-384"),
//            new BCECDSASignatureProvider(CurveType.P384));
//
//    public static final MulticodecDecoder CODECS = MulticodecDecoder.getInstance(
//            KeyCodec.P256_PUBLIC_KEY,
//            KeyCodec.P256_PRIVATE_KEY,
//            KeyCodec.P384_PUBLIC_KEY,
//            KeyCodec.P384_PRIVATE_KEY);
//
//    public ECDSASD2023() {
//        super(CRYPTOSUITE_NAME, Multibase.BASE_64_URL);
//    }
//
//    @Override
//    public ECDSASD2023Issuer createIssuer(KeyPair keyPair) {
//
//        byte[] privateKey = keyPair.privateKey().rawBytes();
//
//        if (privateKey.length == 32) {
//            return new ECDSASD2023Issuer(this, CurveType.P256, CRYPTO_256, keyPair, proofValueBase);
//        }
//        if (privateKey.length == 48) {
//            return new ECDSASD2023Issuer(this, CurveType.P384, CRYPTO_384, keyPair, proofValueBase);
//        }
//        throw new IllegalArgumentException("Usupported key length " + privateKey.length + " bytes, expected 32 bytes (256 bits) or 48 bytes (384 bits).");
//    }
//
//    @Override
//    protected ProofValue getProofValue(Proof proof, DocumentModel model, byte[] proofValue, DocumentLoader loader, URI base) throws DocumentError {
//        if (ECDSASDBaseProofValue.is(proofValue)) {
//            return ECDSASDBaseProofValue.of(proof, model, proofValue, loader);
//        }
//        if (ECDSASDDerivedProofValue.is(proofValue)) {
//            return ECDSASDDerivedProofValue.of(proof, model, proofValue, loader);
//        }
//        throw new DocumentError(ErrorType.Unknown, "ProofValue");
//    }
//
//    @Override
//    protected CryptoSuite getCryptoSuite(String cryptoName, ProofValue proofValue) throws DocumentError {
//        if (!CRYPTOSUITE_NAME.equals(cryptoName)) {
//            return null;
//        }
//
//        if (proofValue != null) {
//            if (proofValue instanceof ECDSASDBaseProofValue baseValue) {
//                return getCryptoSuite(baseValue.baseSignature);
//            }
//            if (proofValue instanceof ECDSASDDerivedProofValue derivedValue) {
//                return getCryptoSuite(derivedValue.baseSignature);
//            }
//        }
//        throw new DocumentError(ErrorType.Unknown, "ProofValue");
//    }
//
//    protected static final CryptoSuite getCryptoSuite(byte[] proofValue) throws DocumentError {
//
//        if (proofValue != null) {
//            if (proofValue.length == 64) {
//                return CRYPTO_256;
//            }
//            if (proofValue.length == 96) {
//                return CRYPTO_384;
//            }
//            throw new DocumentError(ErrorType.Invalid, "ProofValueLength");
//        }
//        throw new DocumentError(ErrorType.Unknown, "ProofValue");
//    }
}