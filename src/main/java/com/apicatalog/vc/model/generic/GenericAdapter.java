package com.apicatalog.vc.model.generic;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;
import com.apicatalog.linkedtree.builder.TreeBuilderError;
import com.apicatalog.linkedtree.fragment.FragmentPropertyError;
import com.apicatalog.linkedtree.jsonld.JsonLdContext;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeReader;
import com.apicatalog.linkedtree.orm.proxy.PropertyValueConsumer;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.model.ProofAdapter;
import com.apicatalog.vc.model.VerifiableModelAdapter;
import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.model.VerifiableModel;
import com.apicatalog.vc.proof.Proof;

import jakarta.json.Json;
import jakarta.json.JsonObject;

public class GenericAdapter implements VerifiableModelAdapter {

    protected final JsonLdTreeReader reader;
    protected final ProofAdapter proofAdapter;

    public GenericAdapter(JsonLdTreeReader reader, ProofAdapter proofAdapter) {
        this.reader = reader;
        this.proofAdapter = proofAdapter;
    }

    @Override
    public Verifiable materialize(VerifiableModel model, DocumentLoader loader, URI base) throws DocumentError {
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
                        new GenericMaterial(
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

        return read(Verifiable.class, model.data(), proofs);
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

}
