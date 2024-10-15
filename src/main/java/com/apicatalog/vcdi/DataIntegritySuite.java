package com.apicatalog.vcdi;

import java.net.URI;
import java.util.Objects;

import com.apicatalog.controller.method.VerificationMethod;
import com.apicatalog.cryptosuite.CryptoSuite;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;
import com.apicatalog.linkedtree.jsonld.JsonLdKeyword;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.method.MethodAdapter;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.proof.ProofValue;
import com.apicatalog.vc.suite.SignatureSuite;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue.ValueType;

public abstract class DataIntegritySuite implements SignatureSuite {

    protected final MethodAdapter methodAdapter;

    protected final String cryptosuiteName;

    protected final Multibase proofValueBase;

//    protected ProofAdapter proofAdapter;

    protected DataIntegritySuite(
            String cryptosuiteName,
            Multibase proofValueBase,
            MethodAdapter method) {
        this.cryptosuiteName = cryptosuiteName;
        this.proofValueBase = proofValueBase;
        this.methodAdapter = method;

    }

//    protected static LinkedLiteralAdapter getProofValueAdapter(Multibase proofValueBase) {
//        //TODO multibase adapter
//        return new LinkedLiteralAdapter() {
//            @Override
//            public LinkedLiteral read(String value, Supplier<LinkedTree> rootSupplier) {
//                return new MultibaseLiteral(datatype(), value, rootSupplier, proofValueBase.decode(value));
//            }
//            
//            @Override
//            public String datatype() {
//                return MultibaseLiteral.TYPE;
//            }
//        };
//    }
//    
    protected abstract ProofValue getProofValue(byte[] proofValue, DocumentLoader loader) throws NodeAdapterError;

    protected abstract CryptoSuite getCryptoSuite(String cryptoName, ProofValue proofValue) throws NodeAdapterError;

    public DataIntegrityProofDraft createDraft(
            VerificationMethod method,
            URI purpose) throws DocumentError {
        return new DataIntegrityProofDraft(this, method, purpose);
    }

    @Override
    public boolean isSupported(Verifiable verifiable, String proofType, JsonObject expandedProof) {
        return VcdiVocab.TYPE.uri().equals(proofType) && cryptosuiteName.equals(getCryptoSuiteName(expandedProof));
    }

    @Override
    public Proof getProof(Verifiable verifiable, JsonObject proof, DocumentLoader loader) throws DocumentError {
        return null;
//
//        var reader = JsonLdTreeReader.createBuilder()
//                .with(
//                        VcdiVocab.TYPE.uri(),
//                        DataIntegrityProof.class,
//                        //TODO remove suite
//                        source -> DataIntegrityProof.of(verifiable, this, source)
//                        )
//                .with(MultibaseLiteral.typeAdapter(proofValueBase))
//                .with(XsdDateTime.typeAdapter())
//                .build();
//
//        
//        try {
//            var tree = reader.read(Json.createArrayBuilder().add(proof).build());
//
//            return tree.materialize(DataIntegrityProof.class);
//
//        } catch (InvalidSelector e) {
//            throw DocumentError.of(e);
//            
//        } catch (TreeBuilderError | NodeAdapterError e) {
//            throw new DocumentError(e, ErrorType.Invalid, "Proof");
//        }
    }

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

    protected static String getCryptoSuiteName(final JsonObject proof) {

        Objects.requireNonNull(proof);

        if (proof.containsKey(VcdiVocab.CRYPTO_SUITE.uri())
                && ValueType.ARRAY == proof.get(VcdiVocab.CRYPTO_SUITE.uri()).getValueType()
                && proof.getJsonArray(VcdiVocab.CRYPTO_SUITE.uri()).size() == 1
                && ValueType.OBJECT == proof.getJsonArray(VcdiVocab.CRYPTO_SUITE.uri()).get(0).getValueType()) {

            final JsonObject valueObject = proof.getJsonArray(VcdiVocab.CRYPTO_SUITE.uri()).getJsonObject(0);

            if (valueObject.containsKey(JsonLdKeyword.TYPE)
                    && ValueType.STRING == valueObject.get(JsonLdKeyword.TYPE).getValueType()
                    && "https://w3id.org/security#cryptosuiteString".equals(valueObject.getString(JsonLdKeyword.TYPE))
                    && valueObject.containsKey(JsonLdKeyword.VALUE)
                    && ValueType.STRING == valueObject.get(JsonLdKeyword.VALUE).getValueType()) {
                return valueObject.getString(JsonLdKeyword.VALUE);
            }

        }
        return null;
    }
}
