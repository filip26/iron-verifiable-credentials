package com.apicatalog.vcdi;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
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
import com.apicatalog.linkedtree.literal.ByteArrayValue;
import com.apicatalog.linkedtree.orm.mapper.TreeReaderMapping;
import com.apicatalog.linkedtree.orm.mapper.TreeReaderMappingBuilder;
import com.apicatalog.linkedtree.orm.proxy.PropertyValueConsumer;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multibase.MultibaseAdapter;
import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.model.generic.GenericMaterial;
import com.apicatalog.vc.proof.GenericSignature;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.proof.ProofValue;
import com.apicatalog.vc.suite.SignatureSuite;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue.ValueType;

public class DataIntegritySuite implements SignatureSuite {

    protected final static DataIntegritySuite GENERIC = new DataIntegritySuite(null, null);

    protected final String cryptosuiteName;

    protected final Multibase proofValueBase;

    protected final Class<? extends DataIntegrityProof> proofInterface;
    
    protected final Collection<Class<?>> customTypes;

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
    }

    public static DataIntegritySuite generic() {
        return GENERIC;
    }

    protected ProofValue getProofValue(VerifiableMaterial verifiable, VerifiableMaterial proof, byte[] proofValue, DocumentLoader loader) throws DocumentError {
        if (proofValue == null || proofValue.length == 0) {
            return null;
        }
        // an unknown signature
        return new GenericSignature();
    }

    protected CryptoSuite getCryptoSuite(String cryptoName, ProofValue proofValue) throws DocumentError {
        if (cryptoName == null) {
            return null;
        }
        // an unknown crypto suite
        return new CryptoSuite(cryptoName, -1, null, null, null);
    }

    public DataIntegrityProofDraft createDraft(VerificationMethod method,
            URI purpose) throws DocumentError {
        throw new UnsupportedOperationException("Generic DI suite does not support createDraft method. Use a signature suite of your choice instead.");
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
    public Proof getProof(VerifiableMaterial verifiable, VerifiableMaterial proofMaterial, DocumentLoader loader) throws DocumentError {

        TreeReaderMappingBuilder builder = TreeReaderMapping.createBuilder()
                .scan(proofInterface, true)
                // TODO supported bases only
                .with(new MultibaseAdapter());

        if (customTypes != null) {
            customTypes.forEach(builder::scan);
        }
        
        TreeReaderMapping mapping = builder.build();

        var reader = JsonLdTreeReader.of(mapping);

        try {
            Proof proof = reader.read(Proof.class, Json.createArrayBuilder().add(proofMaterial.expanded()).build());
            if (proof == null) {
                return null;
            }

            if (proof instanceof PropertyValueConsumer consumer
                    && proof instanceof Linkable linkable) {

                final ByteArrayValue signature = linkable.ld().asFragment()
                        .literal(VcdiVocab.PROOF_VALUE.uri(), ByteArrayValue.class);

                ProofValue proofValue = null;

                if (signature != null) {
                    final VerifiableMaterial unsignedProof = new GenericMaterial(
                            proofMaterial.context(),
                            Json.createObjectBuilder(proofMaterial.compacted())
                                    .remove(VcdiVocab.PROOF_VALUE.name()).build(),
                            Json.createObjectBuilder(proofMaterial.expanded())
                                    .remove(VcdiVocab.PROOF_VALUE.uri()).build());

                    proofValue = getProofValue(verifiable, unsignedProof, signature.byteArrayValue(), loader);
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

                consumer.acceptFragmentPropertyValue("cryptoSuite", cryptoSuite);
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
}
