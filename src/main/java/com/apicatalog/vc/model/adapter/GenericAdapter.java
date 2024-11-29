package com.apicatalog.vc.model.adapter;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;
import com.apicatalog.linkedtree.builder.TreeBuilderError;
import com.apicatalog.linkedtree.fragment.FragmentPropertyError;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeReader;
import com.apicatalog.linkedtree.orm.proxy.PropertyValueConsumer;
import com.apicatalog.vc.adapter.ProofAdapter;
import com.apicatalog.vc.model.DocumentError;
import com.apicatalog.vc.model.DocumentModel;
import com.apicatalog.vc.model.VerifiableDocument;
import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.model.DocumentError.ErrorType;
import com.apicatalog.vc.proof.LinkedProof;
import com.apicatalog.vc.proof.Proof;

import jakarta.json.Json;

public class GenericAdapter implements DocumentAdapter {

    protected final JsonLdTreeReader reader;
    protected final ProofAdapter proofAdapter;

    public GenericAdapter(JsonLdTreeReader reader, ProofAdapter proofAdapter) {
        this.reader = reader;
        this.proofAdapter = proofAdapter;
    }

    @Override
    public VerifiableDocument materialize(DocumentModel model, DocumentLoader loader, URI base) throws DocumentError {
        
        Collection<Proof> proofs = Collections.emptyList();

        if (model.proofs() != null && !model.proofs().isEmpty()) {

            proofs = new ArrayList<>(model.proofs().size());

            for (final VerifiableMaterial proof : model.proofs()) {
                proofs.add(proofAdapter.materialize(
                        model.of(model.data(), List.of(proof)),
                        loader,
                        base));
            }
        }

        return read(VerifiableDocument.class, model.data(), proofs);
    }

    protected <T> T read(Class<T> type, VerifiableMaterial data, Collection<Proof> proofs) throws DocumentError {
        try {
            final T verifiable = reader.read(
                    type,
                    Json.createArrayBuilder().add(data.expanded()).build());

            if (verifiable instanceof PropertyValueConsumer consumer) {
                consumer.acceptFragmentPropertyValue("proofs", proofs);
                
                proofs.stream()
                    .filter(LinkedProof.class::isInstance)
                    .filter(PropertyValueConsumer.class::isInstance)
                    .map(PropertyValueConsumer.class::cast)
                    .forEach(proof -> proof.acceptFragmentPropertyValue("document", verifiable));
                    
            }

            
            return verifiable;

        } catch (FragmentPropertyError e) {
            throw DocumentError.of(e);

        } catch (TreeBuilderError | NodeAdapterError e) {
            throw new DocumentError(e, ErrorType.Invalid, "Document");
        }
    }

}
