package com.apicatalog.vc.integrity;

import java.net.URI;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.vc.method.MethodAdapter;
import com.apicatalog.vc.proof.ProofValue;
import com.apicatalog.vc.suite.SignatureSuite;
import com.apicatalog.vcdm.VcdmVocab;

public abstract class DataIntegritySuite implements SignatureSuite {

    protected static final String PROOF_TYPE_NAME = "DataIntegrityProof";

    protected static final String PROOF_TYPE_ID = VcdmVocab.SECURITY_VOCAB + PROOF_TYPE_NAME;

    protected final MethodAdapter methodAdapter;

    protected final String cryptosuite;

    protected final Multibase proofValueBase;

    protected DataIntegritySuite(
            String cryptosuite,
            Multibase proofValueBase,
            final MethodAdapter method) {
        this.cryptosuite = cryptosuite;
        this.proofValueBase = proofValueBase;
        this.methodAdapter = method;
    }

    protected abstract ProofValue getProofValue(byte[] proofValue, DocumentLoader loader) throws DocumentError;

    protected abstract CryptoSuite getCryptoSuite(String cryptoName, ProofValue proofValue) throws DocumentError;

    public DataIntegrityProofDraft createDraft(
            VerificationMethod method,
            URI purpose) throws DocumentError {
        return new DataIntegrityProofDraft(this, method, purpose);
    }
    
//    @Override
//    public boolean isSupported(String proofType, LinkedNode expandedProof) {
//        return PROOF_TYPE_ID.equals(proofType) && cryptosuite.equals(getCryptoSuiteName(expandedProof));
//    }
//
//    @Override
//    public DataIntegrityProof getProof(LinkedNode expandedProof, DocumentLoader loader) throws DocumentError {
//
//        if (expandedProof == null) {
//            throw new IllegalArgumentException("The 'document' parameter must not be null.");
//        }
//
//        final LdNode node = LdNode.of(expandedProof);
//
//        final String cryptoSuiteName = node.scalar(DataIntegrityVocab.CRYPTO_SUITE).string();
//
//        final byte[] signature = node.scalar(DataIntegrityVocab.PROOF_VALUE).multibase(proofValueBase);
//
//        final ProofValue proofValue = signature != null ? getProofValue(signature, loader) : null;
//
//        CryptoSuite crypto = getCryptoSuite(cryptoSuiteName, proofValue);
//
////FIXME        final DataIntegrityProof proof = new DataIntegrityProof(this, crypto, expandedProof);
//
////        proof.value = proofValue;
////
////        proof.id = node.id();
////
////        proof.created = node.scalar(DataIntegrityVocab.CREATED).xsdDateTime();
////
////        proof.purpose = node.node(DataIntegrityVocab.PURPOSE).id();
////
////        proof.domain = node.scalar(DataIntegrityVocab.DOMAIN).string();
////
////        proof.challenge = node.scalar(DataIntegrityVocab.CHALLENGE).string();
////
////        proof.nonce = node.scalar(DataIntegrityVocab.NONCE).string();
////
////        proof.method = node.node(DataIntegrityVocab.VERIFICATION_METHOD).map(methodAdapter);
////
////        proof.previousProof = node.node(DataIntegrityVocab.PREVIOUS_PROOF).id();
//
//        return null;
//    }

//    protected static String getCryptoSuiteName(final LinkedNode proof) {
//        Objects.requireNonNull(proof);
//
////        final Collection<LinkedData> cryptosuites = proof.values(DataIntegrityVocab.CRYPTO_SUITE.uri());
////
////        if (cryptosuites == null || cryptosuites.isEmpty()) {
////          
////        }
//        return null;
////        try {
////                
////            
////            return LdNode.of(proof).scalar(DataIntegrityVocab.CRYPTO_SUITE).string();
////
////        } catch (DocumentError e) {
////
////        }
////        return null;
//    }
}
