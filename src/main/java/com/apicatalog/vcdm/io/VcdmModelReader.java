package com.apicatalog.vcdm.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.jsonld.JsonLdContext;
import com.apicatalog.linkedtree.jsonld.JsonLdKeyword;
import com.apicatalog.linkedtree.orm.context.ContextReducer;
import com.apicatalog.vc.jsonld.JsonLdMaterialReader;
import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.model.VerifiableMaterialReader;
import com.apicatalog.vc.model.VerifiableModel;
import com.apicatalog.vc.model.VerifiableModelReader;
import com.apicatalog.vc.model.VerifiableModelWriter;
import com.apicatalog.vc.model.generic.GenericMaterial;
import com.apicatalog.vcdm.EmbeddedProof;
import com.apicatalog.vcdm.Vcdm;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.VcdmVocab;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;

public abstract class VcdmModelReader implements VerifiableModelReader, VerifiableModelWriter {

    protected final VcdmVersion readerVersion;
    protected final VerifiableMaterialReader materialReader;
    protected final ContextReducer contextReducer;

    protected VcdmModelReader(VcdmVersion readerVersion, ContextReducer contextReducer) {
        this.readerVersion = readerVersion;
        this.contextReducer = contextReducer;
        this.materialReader = new JsonLdMaterialReader();
    }

    @Override
    public VerifiableModel read(VerifiableMaterial data) throws DocumentError {

        VcdmVersion version = modelVersion(data.context().strings());

        if (version == null || version != readerVersion) {
            throw new DocumentError(ErrorType.Unknown, "DocumentModel");
        }

        final JsonObject expandedUnsigned = EmbeddedProof.removeProofs(data.expanded());

        final Collection<JsonObject> compactedProofs = EmbeddedProof.compactedProofs(data.compacted());

        final JsonObject compactedUnsigned = Json.createObjectBuilder(data.compacted()).remove(VcdmVocab.PROOF.name()).build();

        final Collection<JsonObject> expandedProofs = EmbeddedProof.expandedProofs(data.expanded());

        return new Vcdm(
                version,
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
                JsonLdContext context = defaultContext;

                if (compactedProof.containsKey(JsonLdKeyword.CONTEXT)) {
                    context = JsonLdContext.of(compactedProof, defaultContext);
                    compactedProof = Json.createObjectBuilder(compactedProof).remove(JsonLdKeyword.CONTEXT).build();
                }

                proofs.add(new GenericMaterial(
                        context,
                        compactedProof,
                        expandedProof));
            }

            return proofs;
        }
        throw new IllegalStateException("Inconsistent model - proofs");
    }

    @Override
    public VerifiableMaterial write(VerifiableModel model) throws DocumentError {

        final JsonObject expanded = model.data().expanded();
        JsonObject compacted = model.data().compacted();

        final Collection<JsonLdContext> contexts = new ArrayList<>(1 + (model.proofs() != null ? model.proofs().size() : 0));
        if (model.data().context() != null) {
            contexts.add(model.data().context());
        }

        if (model.proofs() != null && !model.proofs().isEmpty()) {
            if (model.proofs().size() == 1) {

                VerifiableMaterial proof = model.proofs().iterator().next();

                if (proof.context() != null) {
                    contexts.add(proof.context());
                }

                compacted = Json.createObjectBuilder(model.data().compacted())
                        .add(VcdmVocab.PROOF.name(), proof.compacted())
                        .build();

            } else {

                JsonArrayBuilder proofs = Json.createArrayBuilder();

                for (final VerifiableMaterial proof : model.proofs()) {
                    proofs.add(proof.compacted());
                    contexts.add(proof.context());
                }

                compacted = Json.createObjectBuilder(model.data().compacted())
                        .add(VcdmVocab.PROOF.name(), proofs)
                        .build();
            }
        }
        return new GenericMaterial(
                contextReducer.reduce(contexts.toArray(JsonLdContext[]::new)), 
                compacted, 
                expanded);
    }

    protected abstract VcdmVersion modelVersion(Collection<String> context) throws DocumentError;
}
