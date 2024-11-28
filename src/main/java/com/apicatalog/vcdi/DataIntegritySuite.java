package com.apicatalog.vcdi;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.apicatalog.controller.method.VerificationMethod;
import com.apicatalog.cryptosuite.CryptoSuite;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.Linkable;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;
import com.apicatalog.linkedtree.builder.TreeBuilderError;
import com.apicatalog.linkedtree.fragment.FragmentPropertyError;
import com.apicatalog.linkedtree.jsonld.JsonLdKeyword;
import com.apicatalog.linkedtree.jsonld.JsonLdType;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeReader;
import com.apicatalog.linkedtree.jsonld.io.JsonLdWriter;
import com.apicatalog.linkedtree.orm.mapper.TreeReaderMapping;
import com.apicatalog.linkedtree.orm.mapper.TreeReaderMappingBuilder;
import com.apicatalog.linkedtree.orm.proxy.PropertyValueConsumer;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multibase.MultibaseAdapter;
import com.apicatalog.multibase.MultibaseLiteral;
import com.apicatalog.vc.model.DocumentModel;
import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.model.generic.GenericMaterial;
import com.apicatalog.vc.proof.GenericSignature;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.proof.ProofValue;
import com.apicatalog.vc.suite.SignatureSuite;
import com.apicatalog.vcdm.VcdmVocab;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue.ValueType;

public class DataIntegritySuite implements SignatureSuite {

    protected final static DataIntegritySuite GENERIC = new DataIntegritySuite(null, null);

    protected final String cryptosuiteName;

    protected final Multibase proofValueBase;

    protected final Class<? extends DataIntegrityProof> proofInterface;

    protected final Collection<Class<?>> customTypes;

    protected final JsonLdWriter writer;
    
    protected DataIntegritySuite(
            String cryptosuiteName,
            Multibase proofValueBase) {
        this(cryptosuiteName, DataIntegrityProof.class, Collections.emptyList(), proofValueBase);
    }

    protected DataIntegritySuite(
            String cryptosuiteName,
            Class<? extends DataIntegrityProof> proofInterface,
            Collection<Class<?>> customTypes,
            Multibase proofValueBase) {
        this.cryptosuiteName = cryptosuiteName;
        this.proofInterface = proofInterface;
        this.customTypes = customTypes;
        this.proofValueBase = proofValueBase;
        this.writer = getWriter(customTypes);
    }

    public static DataIntegritySuite generic() {
        return GENERIC;
    }

    protected ProofValue getProofValue(Proof proof, DocumentModel model, byte[] proofValue, DocumentLoader loader, URI base) throws DocumentError {
        if (proofValue == null || proofValue.length == 0) {
            return null;
        }
        // an unknown signature
        return new GenericSignature(proof);
    }

    protected CryptoSuite getCryptoSuite(String cryptoName, ProofValue proofValue) throws DocumentError {
        if (cryptoName == null) {
            return null;
        }
        // an unknown crypto suite
        return new CryptoSuite(cryptoName, -1, null, null, null);
    }

    @Override
    public boolean isSupported(VerifiableMaterial verifiable, VerifiableMaterial proofMaterial) {

        Collection<String> proofType = JsonLdType.strings(proofMaterial.expanded());

        if (proofType == null || proofType.isEmpty()) {
            return false;
        }

        final JsonObject expandedProof = proofMaterial.expanded();

        return proofType.contains(VcdiVocab.TYPE.uri()) &&
                (cryptosuiteName == null
                        || cryptosuiteName.equals(getCryptoSuiteName(expandedProof)));
    }

    @Override
    public Proof getProof(DocumentModel model, DocumentLoader loader, URI base) throws DocumentError {

        TreeReaderMappingBuilder builder = TreeReaderMapping.createBuilder()
                .scan(proofInterface, true)
                .with(new MultibaseAdapter());

        if (customTypes != null) {
            customTypes.forEach(builder::scan);
        }

        TreeReaderMapping mapping = builder.build();

        var reader = JsonLdTreeReader.of(mapping);

        VerifiableMaterial proofMaterial = model.proofs().iterator().next();
        
        try {
            Proof proof = reader.read(Proof.class, Json.createArrayBuilder().add(proofMaterial.expanded()).build());
            if (proof == null) {
                return null;
            }

            if (proof instanceof PropertyValueConsumer consumer) {

                if (proof instanceof DataIntegrityProof) {
                    consumer.acceptFragmentPropertyValue("di", this);
                }

                if (proof instanceof Linkable linkable) {

                    final MultibaseLiteral signature = linkable.ld().asFragment()
                            .literal(VcdiVocab.PROOF_VALUE.uri(), MultibaseLiteral.class);

                    ProofValue proofValue = null;

                    if (signature != null) {

                        if (proofValueBase != null && !proofValueBase.equals(signature.base())) {
                            throw new DocumentError(ErrorType.Invalid, VcdiVocab.PROOF_VALUE.name() + "Multibase");
                        }

                        final VerifiableMaterial unsignedProof = new GenericMaterial(
                                proofMaterial.context(),
                                Json.createObjectBuilder(proofMaterial.compacted())
                                        .remove(VcdiVocab.PROOF_VALUE.name()).build(),
                                Json.createObjectBuilder(proofMaterial.expanded())
                                        .remove(VcdiVocab.PROOF_VALUE.uri()).build());

                        proofValue = getProofValue(proof, model.of(model.data(), List.of(unsignedProof)), signature.byteArrayValue(), loader, base);
                        consumer.acceptFragmentPropertyValue("signature", proofValue);
                    }

                    CryptoSuite cryptoSuite = null;

                    if (proofValue == null) {
                        cryptoSuite = DataIntegritySuite.this.getCryptoSuite(
                                cryptosuiteName == null
                                        ? getCryptoSuiteName(proofMaterial.expanded())
                                        : cryptosuiteName,
                                proofValue);
                    } else {
                        cryptoSuite = getCryptoSuite(
                                cryptosuiteName == null
                                        ? getCryptoSuiteName(proofMaterial.expanded())
                                        : cryptosuiteName,
                                proofValue);
                    }

                    consumer.acceptFragmentPropertyValue("cryptosuite", cryptoSuite);
                }
            }

            return proof;

        } catch (FragmentPropertyError e) {
            throw new DocumentError(e, ErrorType.Invalid, e.getPropertyName());

        } catch (TreeBuilderError e) {
            if (e.term() != null) {
                throw new DocumentError(e, ErrorType.Invalid, e.term());
            }
            throw new DocumentError(e, ErrorType.Invalid, "Proof", e.term());

        } catch (NodeAdapterError e) {
            throw new DocumentError(e, ErrorType.Invalid, "Proof");
        }

    }

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
    
    protected static JsonLdWriter getWriter(Collection<Class<?>> customTypes) {
        JsonLdWriter writer = new JsonLdWriter()
                .scan(DataIntegrityProof.class)
                .scan(VerificationMethod.class);

        if (customTypes != null) {
            customTypes.forEach(writer::scan);
        }

        // context reducer
        writer.context(VcdmVocab.CONTEXT_MODEL_V2,
                List.of(VcdiVocab.CONTEXT_MODEL_V2));

        return writer;
    }
}
