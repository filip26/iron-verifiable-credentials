package com.apicatalog.di.suite;

import java.security.MessageDigest;
import java.security.SignatureException;
import java.util.function.Function;

import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.di.proof.DataIntegrityProof.Builder;
import com.apicatalog.di.sd.SDBaseProofValue;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.trust.model.DataModel;
import com.apicatalog.trust.payload.RedactablePayload;
import com.apicatalog.trust.processor.PayloadProcessor;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.signature.Signature;

public final class ECDSASD2023 implements CryptoSuite {

    public static final String P256 = "P-256";
    public static final String P384 = "P-384";

    private static final String CRYPTOSUITE_NAME = "ecdsa-sd-2023";

    private static ECDSASD2023 INSTANCE = new ECDSASD2023();

    private ECDSASD2023() {
    }

    public static ECDSASD2023 getInstance() {
        return INSTANCE;
    }
    
    public DataIntegrityProof sign(
            String algorithm,
            AsymmetricSigner baseSigner,
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
                proofSigner,
                digestor,
                unsignedProof,
                payload);

//        var signature = signatureGenerator.generate(
//                algorithm,
//                signer,
//                digestFactory,
//                unsigned,
//                payload);

        proofDraft.build(signature);
        return proofDraft.snapshot();

    }

    @Override
    public String id() {
        return CRYPTOSUITE_NAME;
    }

    @Override
    public String c14n() {
        return DataModel.C14N_RDFC;
    }

    @Override
    public String encode(Signature signature) {
        return Multibase.BASE_64_URL.encode(signature.toByteArray());
    }

    @Override
    public Signature decode(String value, Proof proof, PayloadProcessor data) {

        var signature = Multibase.BASE_64_URL.decode(value);
//        IO.println("SD sig length: " + signature.length);
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

        if (SDBaseProofValue.isAccepted(signature)) {
            return SDBaseProofValue.decode(
                    signatureAlgorithm,
                    digestAlgorithm,
                    signature,
                    proof,
                    data);
        }

        throw new IllegalArgumentException();
    }
    
    
//    public 
//    
//    public static class ProofDraft {
//        
//    }

//    protected ProofValue getProofValue(Proof proof, DocumentModel model, byte[] proofValue, DocumentLoader loader, URI base) throws DocumentError {
//        if (ECDSASDBaseProofValue.is(proofValue)) {
//            return ECDSASDBaseProofValue.of(proof, model, proofValue, loader);
//        }
//        if (ECDSASDDerivedProofValue.is(proofValue)) {
//            return ECDSASDDerivedProofValue.of(proof, model, proofValue, loader);
//        }
//        throw new DocumentError(ErrorType.Unknown, "ProofValue");
//    }

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