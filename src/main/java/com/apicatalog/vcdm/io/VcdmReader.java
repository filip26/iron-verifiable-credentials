package com.apicatalog.vcdm.io;

import java.util.Collection;
import java.util.Collections;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.jsonld.JsonLdMaterialReader;
import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.model.VerifiableMaterialReader;
import com.apicatalog.vc.model.VerifiableModel;
import com.apicatalog.vc.model.VerifiableModelReader;
import com.apicatalog.vc.reader.VerifiableReader;
import com.apicatalog.vcdm.EmbeddedProof;
import com.apicatalog.vcdm.Vcdm;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.VcdmVocab;

import jakarta.json.Json;
import jakarta.json.JsonObject;

/**
 * A W3C Verifiable Credentials Data Model JSON-LD based reader.
 */
public abstract class VcdmReader implements VerifiableModelReader, VerifiableReader {

    protected final VerifiableMaterialReader materialReader;
    protected final VcdmVersion version;

    protected VcdmReader(VcdmVersion version) {
        this.version = version;
        this.materialReader = new JsonLdMaterialReader();
    }

    @Override
    public VerifiableModel read(VerifiableMaterial data) throws DocumentError {

        final JsonObject expandedUnsigned = EmbeddedProof.removeProofs(data.expanded());

        final Collection<JsonObject> compactedProofs = EmbeddedProof.compactedProofs(data.compacted());

        final JsonObject compactedUnsigned = Json.createObjectBuilder(data.compacted()).remove(VcdmVocab.PROOF.name()).build();

        final Collection<JsonObject> expandedProofs = EmbeddedProof.expandedProofs(data.expanded());

        return new Vcdm(
                version,
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
