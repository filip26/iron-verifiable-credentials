package com.apicatalog.vcdm.io;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;
import com.apicatalog.linkedtree.builder.TreeBuilderError;
import com.apicatalog.linkedtree.fragment.FragmentPropertyError;
import com.apicatalog.linkedtree.jsonld.JsonLdContext;
import com.apicatalog.linkedtree.jsonld.JsonLdKeyword;
import com.apicatalog.linkedtree.jsonld.JsonLdType;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeReader;
import com.apicatalog.linkedtree.orm.proxy.PropertyValueConsumer;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.Presentation;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.model.ProofAdapter;
import com.apicatalog.vc.model.VerifiableAdapter;
import com.apicatalog.vc.model.VerifiableAdapterProvider;
import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.model.VerifiableModel;
import com.apicatalog.vc.model.VerifiableModelReader;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vcdm.Vcdm;
import com.apicatalog.vcdm.VcdmVocab;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public class VcdmAdapter implements VerifiableAdapter {

    protected final ProofAdapter proofAdapter;
    protected final VerifiableModelReader credentialModelReader;
    protected final VerifiableAdapterProvider credentialAdapterProvider;

    protected final JsonLdTreeReader reader;

    public VcdmAdapter(
            JsonLdTreeReader reader,
            VerifiableAdapterProvider credentialAdapterProvider,
            VerifiableModelReader credentialModelReader,
            ProofAdapter proofMaterializer) {
        this.reader = reader;
        this.credentialAdapterProvider = credentialAdapterProvider;
        this.credentialModelReader = credentialModelReader;
        this.proofAdapter = proofMaterializer;
    }

    @Override
    public Verifiable materialize(VerifiableModel model, DocumentLoader loader, URI base) throws DocumentError {

        final Collection<String> types = JsonLdType.strings(model.data().expanded());

        if (types == null || types.isEmpty()) {
            throw new DocumentError(ErrorType.Missing, JsonLdKeyword.TYPE);
        }

        Collection<Proof> proofs = Collections.emptyList();

        if (model.expandedProofs() != null && model.compactedProofs() != null) {

            if (model.expandedProofs().size() != model.compactedProofs().size()) {
                throw new IllegalStateException("Inconsistent model - proof size");
            }

            proofs = new ArrayList<>(model.expandedProofs().size());

            Iterator<JsonObject> itCompactedProofs = model.compactedProofs().iterator();

            for (JsonObject expandedProof : model.expandedProofs()) {

                JsonObject compactedProof = itCompactedProofs.next();

                proofs.add(proofAdapter.materialize(
                        model.data(),
                        new VerifiableMaterial(
                                JsonLdContext.strings(compactedProof, model.data().context()),
                                compactedProof,
                                expandedProof),
                        loader,
                        base));
            }

        } else if ((model.expandedProofs() != null && !model.expandedProofs().isEmpty())
                || (model.compactedProofs() != null && !model.compactedProofs().isEmpty())) {
            throw new IllegalStateException("Inconsistent model - proofs");
        }

        if (isCredential(types)) {
            return read(Credential.class, model.data(), proofs);

        } else if (isPresentation(types)) {
            return readPresentation(model.data(), proofs, loader, base);
        }

        throw new DocumentError(ErrorType.Unknown, JsonLdKeyword.TYPE);
    }

    protected Presentation readPresentation(
            VerifiableMaterial data,
            Collection<Proof> proofs,
            DocumentLoader loader,
            URI base) throws DocumentError {
        
        Presentation presentation = read(Presentation.class, data, proofs);

        if (presentation instanceof PropertyValueConsumer consumer) {
            consumer.acceptFragmentPropertyValue("credentials", credentials(data, loader, base));
        }

        return presentation;
    }

    protected <T> T read(Class<T> type, VerifiableMaterial data, Collection<Proof> proofs) throws DocumentError {
        try {
            final T verifiable = reader.read(
                    type,
                    Json.createArrayBuilder().add(data.expanded()).build());

            if (verifiable instanceof PropertyValueConsumer consumer) {
                consumer.acceptFragmentPropertyValue("proofs", proofs);
            }

            return verifiable;

        } catch (FragmentPropertyError e) {
            throw DocumentError.of(e);

        } catch (TreeBuilderError | NodeAdapterError e) {
            throw new DocumentError(e, ErrorType.Invalid, "Document");
        }
    }

    protected Credential credential(VerifiableMaterial data, DocumentLoader loader, URI base) throws DocumentError {

        VerifiableModel model = credentialModelReader.read(data);

        if (model instanceof Vcdm) {

            VerifiableAdapter adapter = credentialAdapterProvider.adapter(model.data().context());

            if (adapter == null) {
                throw new DocumentError(ErrorType.Invalid, "CredentialModel");
            }

            Verifiable verifiable = adapter.materialize(model, loader, base);

            if (verifiable instanceof Credential credential) {
                return credential;
            }
        }

        throw new DocumentError(ErrorType.Invalid, "CredentialModel");
    }

    protected Collection<Credential> credentials(VerifiableMaterial data, DocumentLoader loader, URI base) throws DocumentError {

        Collection<JsonObject> expanded = expandedCredentials(data.expanded().get(VcdmVocab.VERIFIABLE_CREDENTIALS.uri()));

        Collection<JsonObject> compacted = compactedCredentials(data.compacted().get(VcdmVocab.VERIFIABLE_CREDENTIALS.name()));

        if (expanded.size() != compacted.size()) {
            throw new IllegalStateException("Incosistent presentation verifiable credentials size, compacted vs expanded.");
        }

        if (expanded.isEmpty()) {
            return Collections.emptyList();
        }

        Collection<Credential> credentials = new ArrayList<>(expanded.size());

        Iterator<JsonObject> itc = compacted.iterator();

        for (JsonObject expandedCredential : expanded) {
            
            JsonObject compactedCredential = itc.next();
            
            credentials.add(credential(new VerifiableMaterial(
                    JsonLdContext.strings(compactedCredential, data.context()),
                    compactedCredential,
                    expandedCredential),
                    loader,
                    base));
        }

        return credentials;
    }

    protected boolean isCredential(Collection<String> compactedType) {
        return compactedType.contains(VcdmVocab.CREDENTIAL_TYPE.uri());
    }

    protected boolean isPresentation(Collection<String> compactedType) {
        return compactedType.contains(VcdmVocab.PRESENTATION_TYPE.uri());
    }

    protected static Collection<JsonObject> expandedCredentials(JsonValue value) throws DocumentError {

        if (value == null) {
            return Collections.emptyList();
        }

        final Collection<JsonValue> container = JsonUtils.toCollection(value);

        if (container.isEmpty()) {
            return Collections.emptyList();
        }

        final Collection<JsonObject> objects = container.stream()
                .filter(JsonUtils::isObject)
                .map(JsonObject.class::cast)
                .map(o -> o.get(JsonLdKeyword.GRAPH))
                .filter(JsonUtils::isArray)
                .map(JsonArray.class::cast)
                .filter(a -> a.size() == 1)
                .map(a -> a.iterator().next())
                .filter(JsonUtils::isObject)
                .map(JsonObject.class::cast)
                .toList();

        if (container.size() != objects.size()) {
            throw new DocumentError(ErrorType.Invalid, VcdmVocab.VERIFIABLE_CREDENTIALS);
        }

        return objects;
    }

    protected static Collection<JsonObject> compactedCredentials(JsonValue value) throws DocumentError {
        if (value == null) {
            return Collections.emptyList();
        }

        final Collection<JsonValue> container = JsonUtils.toCollection(value);

        if (container.isEmpty()) {
            return Collections.emptyList();
        }

        final Collection<JsonObject> objects = container.stream()
                .filter(JsonUtils::isObject)
                .map(JsonObject.class::cast)
                .toList();

        if (container.size() != objects.size()) {
            throw new DocumentError(ErrorType.Invalid, VcdmVocab.VERIFIABLE_CREDENTIALS);
        }

        return objects;
    }

    protected static Collection<String> compactedType(JsonObject document) {
        JsonValue value = document.get("type");
        if (JsonUtils.isNotNull(value)) {
            return JsonUtils.toJsonArray(value).stream()
                    .filter(JsonUtils::isString)
                    .map(JsonString.class::cast)
                    .map(JsonString::getString)
                    .toList();
        }
        return Collections.emptySet();
    }
}
