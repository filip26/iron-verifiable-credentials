package com.apicatalog.vc.model;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeReader;
import com.apicatalog.linkedtree.orm.mapper.TreeReaderMapping;
import com.apicatalog.linkedtree.orm.mapper.TreeReaderMappingBuilder;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.jsonld.JsonLdMaterialReader;
import com.apicatalog.vcdm.EmbeddedProof;
import com.apicatalog.vcdm.VcdmVocab;

import jakarta.json.Json;
import jakarta.json.JsonObject;

public class GenericVerifiableReader implements VerifiableModelReader, VerifiableReader {

    protected final VerifiableAdapter adapter;
    protected final ProofAdapter proofAdapter;
    protected final VerifiableMaterialReader materialReader;

    protected GenericVerifiableReader(final JsonLdTreeReader reader, ProofAdapter proofAdapter) {
        this.adapter = null;// new VcdmAdapter(reader, this, this, proofAdapter);
        this.proofAdapter = proofAdapter;
        this.materialReader = new JsonLdMaterialReader();
    }

    public static GenericVerifiableReader with(final ProofAdapter proofAdapter) {
        return with(proofAdapter, new Class[0]);
    }

    public static GenericVerifiableReader with(
            final ProofAdapter proofAdapter,
            final Class<?>... types) {

        Objects.requireNonNull(types);

        TreeReaderMappingBuilder builder = TreeReaderMapping.createBuilder();

        for (Class<?> type : types) {
            builder.scan(type);
        }

        builder.scan(Verifiable.class, true);

        return new GenericVerifiableReader(JsonLdTreeReader.of(builder.build()), proofAdapter);
    }

    @Override
    public Verifiable read(JsonObject document, DocumentLoader loader, URI base) throws DocumentError {
        VerifiableMaterial material = materialReader.read(Collections.emptyList(), document, loader, base);

        VerifiableModel model = read(material);

        return adapter.materialize(model, loader, base);
    }

    @Override
    public VerifiableModel read(VerifiableMaterial data) throws DocumentError {

        final JsonObject expandedUnsigned = EmbeddedProof.removeProofs(data.expanded());

        final Collection<JsonObject> compactedProofs = EmbeddedProof.compactedProofs(data.compacted());

        final JsonObject compactedUnsigned = Json.createObjectBuilder(data.compacted()).remove(VcdmVocab.PROOF.name()).build();

        final Collection<JsonObject> expandedProofs = EmbeddedProof.expandedProofs(data.expanded());

        return new GenericVerifiableModel(
                new VerifiableMaterial(
                        data.context(),
                        compactedUnsigned,
                        expandedUnsigned),
                compactedProofs != null
                        ? compactedProofs
                        : Collections.emptyList(),
                expandedProofs != null
                        ? expandedProofs
                        : Collections.emptyList());
    }

}
