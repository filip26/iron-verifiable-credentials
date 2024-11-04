package com.apicatalog.vc.di;

import java.net.URI;
import java.util.Objects;

import com.apicatalog.controller.method.VerificationMethod;
import com.apicatalog.cryptosuite.CryptoSuite;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.Linkable;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;
import com.apicatalog.linkedtree.builder.TreeBuilderError;
import com.apicatalog.linkedtree.jsonld.JsonLdKeyword;
import com.apicatalog.linkedtree.jsonld.io.JsonLdReader;
import com.apicatalog.linkedtree.literal.ByteArrayValue;
import com.apicatalog.linkedtree.orm.mapper.TreeReaderMapping;
import com.apicatalog.linkedtree.orm.proxy.PropertyValueConsumer;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multibase.MultibaseAdapter;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.proof.ProofValue;
import com.apicatalog.vc.suite.SignatureSuite;
import com.apicatalog.vc.verifier.VerifiableMaterial;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue.ValueType;

public abstract class DataIntegritySuite implements SignatureSuite {

//    protected final MethodAdapter methodAdapter;

    protected final String cryptosuiteName;

    protected final Multibase proofValueBase;

//    protected ProofAdapter proofAdapter;

    protected DataIntegritySuite(
            String cryptosuiteName,
            Multibase proofValueBase) {
        this.cryptosuiteName = cryptosuiteName;
        this.proofValueBase = proofValueBase;
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
    protected abstract ProofValue getProofValue(VerifiableMaterial verifiable, VerifiableMaterial proof, byte[] proofValue, DocumentLoader loader) throws DocumentError;

    protected abstract CryptoSuite getCryptoSuite(String cryptoName, ProofValue proofValue) throws DocumentError;

    public DataIntegrityProofDraft createDraft(
            VerificationMethod method,
            URI purpose) throws DocumentError {
        return new DataIntegrityProofDraft(this, method, purpose);
    }

    @Override
    public boolean isSupported(VerifiableMaterial verifiable, String proofType, JsonObject expandedProof) {
        return VcdiVocab.TYPE.uri().equals(proofType) && cryptosuiteName.equals(getCryptoSuiteName(expandedProof));
    }

    @Override
    public Proof getProof(VerifiableMaterial verifiable, JsonObject expanded, DocumentLoader loader) throws DocumentError {

        var mapping = TreeReaderMapping.createBuilder()
                .scan(DataIntegrityProof.class)
                .with(new MultibaseAdapter())   //TODO supported bases only
                // TODO add custom type -> custom mapper
                .build();

        var reader = JsonLdReader.of(mapping, loader);

        try {
            Proof proof = reader.read(Proof.class, expanded);
            if (proof == null) {
                return null;
            }

            if (proof instanceof PropertyValueConsumer consumer
                    && proof instanceof Linkable linkable) {

                final ByteArrayValue signature = linkable.ld().asFragment()
                        .literal(VcdiVocab.PROOF_VALUE.uri(), ByteArrayValue.class);

                if (signature != null) {
                    VerifiableMaterial proofMaterial = new VerifiableMaterial(linkable.ld().asFragment().root(), null, Json.createArrayBuilder().add(expanded).build(), null);
                            
                    ProofValue proofValue = getProofValue(verifiable, proofMaterial, signature.byteArrayValue(), loader);
                    consumer.acceptFragmentPropertyValue("signature", proofValue);

                    if (proofValue != null) {
                        CryptoSuite cryptoSuite = getCryptoSuite(cryptosuiteName, proofValue);
                        consumer.acceptFragmentPropertyValue("cryptoSuite", cryptoSuite);
                    }
                }
            }

            return proof;

        } catch (TreeBuilderError | NodeAdapterError e) {
            throw new DocumentError(e, ErrorType.Invalid, "Proof");
        }
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

    protected static String getCryptoSuiteName(final JsonObject expandedProof) {

        Objects.requireNonNull(expandedProof);

        if (expandedProof.containsKey(VcdiVocab.CRYPTO_SUITE.uri())
                && ValueType.ARRAY == expandedProof.get(VcdiVocab.CRYPTO_SUITE.uri()).getValueType()
                && expandedProof.getJsonArray(VcdiVocab.CRYPTO_SUITE.uri()).size() == 1
                && ValueType.OBJECT == expandedProof.getJsonArray(VcdiVocab.CRYPTO_SUITE.uri()).get(0).getValueType()) {

            final JsonObject valueObject = expandedProof.getJsonArray(VcdiVocab.CRYPTO_SUITE.uri()).getJsonObject(0);

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
