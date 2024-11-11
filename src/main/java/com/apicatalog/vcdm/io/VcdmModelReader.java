package com.apicatalog.vcdm.io;

import java.util.Collection;
import java.util.Collections;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.vc.jsonld.JsonLdMaterialReader;
import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.model.VerifiableMaterialReader;
import com.apicatalog.vc.model.VerifiableModel;
import com.apicatalog.vc.model.VerifiableModelReader;
import com.apicatalog.vc.model.generic.GenericMaterial;
import com.apicatalog.vcdm.EmbeddedProof;
import com.apicatalog.vcdm.Vcdm;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.VcdmVocab;

import jakarta.json.Json;
import jakarta.json.JsonObject;

public abstract class VcdmModelReader implements VerifiableModelReader {

    protected final VcdmVersion readerVersion;
    protected final VerifiableMaterialReader materialReader;

    protected VcdmModelReader(VcdmVersion readerVersion) {
        this.readerVersion = readerVersion;
        this.materialReader = new JsonLdMaterialReader();
    }

    @Override
    public VerifiableModel read(VerifiableMaterial data) throws DocumentError {
        
        VcdmVersion version = modelVersion(data.context());
        
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
                compactedProofs != null
                        ? compactedProofs
                        : Collections.emptyList(),
                expandedProofs != null
                        ? expandedProofs
                        : Collections.emptyList());
    }
    
    protected abstract VcdmVersion modelVersion(Collection<String> context) throws DocumentError;
}
