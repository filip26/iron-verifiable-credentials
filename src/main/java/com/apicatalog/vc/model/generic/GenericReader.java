package com.apicatalog.vc.model.generic;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.jsonld.JsonLdContext;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeReader;
import com.apicatalog.linkedtree.orm.mapper.TreeReaderMapping;
import com.apicatalog.linkedtree.orm.mapper.TreeReaderMappingBuilder;
import com.apicatalog.vc.VerifiableDocument;
import com.apicatalog.vc.jsonld.JsonLdMaterialReader;
import com.apicatalog.vc.model.ProofAdapter;
import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.model.VerifiableMaterialReader;
import com.apicatalog.vc.model.DocumentModel;
import com.apicatalog.vc.model.DocumentAdapter;
import com.apicatalog.vc.model.DocumentModelReader;
import com.apicatalog.vc.model.DocumentModelWriter;
import com.apicatalog.vc.model.DocumentModelAdapter;
import com.apicatalog.vcdm.EmbeddedProof;
import com.apicatalog.vcdm.VcdmVocab;

import jakarta.json.Json;
import jakarta.json.JsonObject;

public class GenericReader implements DocumentModelReader, DocumentModelWriter, DocumentModelAdapter {

    protected final DocumentAdapter adapter;
    protected final VerifiableMaterialReader materialReader;

    protected GenericReader(final DocumentAdapter adapter, VerifiableMaterialReader reader) {
        this.adapter = adapter;
        this.materialReader = reader;
    }

    public static GenericReader with(final ProofAdapter proofAdapter) {
        return with(proofAdapter, new Class[0]);
    }

    public static GenericReader with(
            final ProofAdapter proofAdapter,
            final Class<?>... types) {

        Objects.requireNonNull(types);

        TreeReaderMappingBuilder builder = TreeReaderMapping.createBuilder();

        for (Class<?> type : types) {
            builder.scan(type);
        }

        builder.scan(VerifiableDocument.class, true);

        return new GenericReader(
                new GenericAdapter(JsonLdTreeReader.of(builder.build()), proofAdapter),
                new JsonLdMaterialReader());
    }

    @Override
    public VerifiableDocument materialize(DocumentModel model, DocumentLoader loader, URI base) throws DocumentError {
        return adapter.materialize(model, loader, base);
    }

    @Override
    public DocumentModel read(JsonObject document, DocumentLoader loader, URI base) throws DocumentError {
        VerifiableMaterial material = materialReader.read(document, loader, base);

        DocumentModel model = read(material);

        return model;
    }

    @Override
    public DocumentModel read(VerifiableMaterial data) throws DocumentError {

        final JsonObject expandedUnsigned = EmbeddedProof.removeProofs(data.expanded());

        final Collection<JsonObject> compactedProofs = EmbeddedProof.compactedProofs(data.compacted());

        final JsonObject compactedUnsigned = Json.createObjectBuilder(data.compacted()).remove(VcdmVocab.PROOF.name()).build();

        final Collection<JsonObject> expandedProofs = EmbeddedProof.expandedProofs(data.expanded());

        return new GenericModel(
                new GenericMaterial(
                        data.context(),
                        compactedUnsigned,
                        expandedUnsigned),
                proofs(
                        data.context(),
                        compactedProofs != null
                                ? compactedProofs
                                : Collections.emptyList(),
                        expandedProofs != null
                                ? expandedProofs
                                : Collections.emptyList()),
                this::write);
    }

    protected Collection<VerifiableMaterial> proofs(
            JsonLdContext defaultContext,
            Collection<JsonObject> compactedProofs,
            Collection<JsonObject> expandedProofs) {

        if (expandedProofs != null && compactedProofs != null) {

            if (expandedProofs.size() != compactedProofs.size()) {
                throw new IllegalStateException("Inconsistent model - proof size");
            }

            Collection<VerifiableMaterial> proofs = new ArrayList<>(expandedProofs.size());

            Iterator<JsonObject> itCompactedProofs = compactedProofs.iterator();

            for (JsonObject expandedProof : expandedProofs) {

                JsonObject compactedProof = itCompactedProofs.next();

                proofs.add(new GenericMaterial(
                        JsonLdContext.of(compactedProof, defaultContext),
                        compactedProof,
                        expandedProof));
            }

            return proofs;
        }
        throw new IllegalStateException("Inconsistent model - proofs");
    }

    @Override
    public VerifiableMaterial write(DocumentModel model) throws DocumentError {

//        JsonObject expanded = model.expandedProofs().isEmpty()
//                ? model.data().expanded()
//                : EmbeddedProof.setProofs(model.data().expanded(), model.expandedProofs());
//
//        JsonObject compacted = model.compactedProofs().isEmpty()
//                ? model.data().compacted()
//                : Json.createObjectBuilder(model.data().compacted()).add(VcdmVocab.PROOF.name(), Json.createArrayBuilder(model.compactedProofs())).build();
//
//        return new GenericMaterial(model.data().context(), compacted, expanded);
        return null;
    }
}
