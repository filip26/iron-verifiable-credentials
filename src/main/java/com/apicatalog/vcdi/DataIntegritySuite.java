package com.apicatalog.vcdi;

import java.net.URI;
import java.util.List;
import java.util.function.Supplier;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.linkedtree.LinkedLiteral;
import com.apicatalog.linkedtree.LinkedTree;
import com.apicatalog.linkedtree.adapter.LinkedLiteralAdapter;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.vc.lt.MultibaseLiteral;
import com.apicatalog.vc.method.MethodAdapter;
import com.apicatalog.vc.proof.ProofAdapter;
import com.apicatalog.vc.proof.ProofValue;
import com.apicatalog.vc.suite.SignatureSuite;

public abstract class DataIntegritySuite implements SignatureSuite {

    protected final MethodAdapter methodAdapter;

    protected final String cryptosuiteName;

    protected final Multibase proofValueBase;
    
    protected ProofAdapter proofAdapter;

    protected DataIntegritySuite(
            String cryptosuiteName,
            Multibase proofValueBase,
            MethodAdapter method
            ) {
        this.cryptosuiteName = cryptosuiteName;
        this.proofValueBase = proofValueBase;
        this.methodAdapter = method;
        this.proofAdapter = new DataIntegrityProofAdapter(this, List.of(getProofValueAdapter(proofValueBase)));
    }

    protected static LinkedLiteralAdapter getProofValueAdapter(Multibase proofValueBase) {
        //TODO multibase adapter
        return new LinkedLiteralAdapter() {
            @Override
            public LinkedLiteral read(String value, Supplier<LinkedTree> rootSupplier) {
                return new MultibaseLiteral(datatype(), value, rootSupplier, proofValueBase.decode(value));
            }
            
            @Override
            public String datatype() {
                return MultibaseLiteral.TYPE;
            }
        };
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
    
    @Override
    public ProofAdapter proofAdapter() {
        return proofAdapter;
    }
}
