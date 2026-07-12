package com.apicatalog.di.sd.legacy;

public final class ECDSASD2023 {

    public static final String CRYPTOSUITE_NAME = "ecdsa-sd-2023";

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