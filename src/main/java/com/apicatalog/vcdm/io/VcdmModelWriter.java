package com.apicatalog.vcdm.io;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.vc.jsonld.JsonLdMaterialReader;
import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.model.VerifiableMaterialReader;
import com.apicatalog.vc.model.VerifiableModel;
import com.apicatalog.vc.model.VerifiableModelWriter;
import com.apicatalog.vc.model.generic.GenericMaterial;
import com.apicatalog.vcdm.EmbeddedProof;
import com.apicatalog.vcdm.Vcdm;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.VcdmVocab;

import jakarta.json.Json;
import jakarta.json.JsonObject;

public abstract class VcdmModelWriter implements VerifiableModelWriter {

    protected final VcdmVersion readerVersion;
    protected final VerifiableMaterialReader materialReader;

    protected VcdmModelWriter(VcdmVersion readerVersion) {
        this.readerVersion = readerVersion;
        this.materialReader = new JsonLdMaterialReader();
    }

    @Override
    public VerifiableMaterial write(VerifiableModel model) throws DocumentError {

        if (model != null && model instanceof Vcdm vcdm && readerVersion == vcdm.version()) {

            JsonObject expanded = EmbeddedProof.setProofs(model.data().expanded(), model.expandedProofs());
            JsonObject compacted = Json.createObjectBuilder(model.data().compacted()).add(VcdmVocab.PROOF.name(), Json.createArrayBuilder(model.compactedProofs())).build();

            return new GenericMaterial(
                    model.data().context(), // ? proof contexts?
                    compacted,
                    expanded);
        }
        throw new DocumentError(ErrorType.Unknown, "DocumentModel");

//        VcdmVersion version = modelVersion(data.context());
//        
//        if (version == null || version != readerVersion) {
//            throw new DocumentError(ErrorType.Unknown, "DocumentModel");
//        }
//        
//        final JsonObject expandedUnsigned = EmbeddedProof.removeProofs(data.expanded());
//
//        final Collection<JsonObject> compactedProofs = EmbeddedProof.compactedProofs(data.compacted());
//
//        final JsonObject compactedUnsigned = Json.createObjectBuilder(data.compacted()).remove(VcdmVocab.PROOF.name()).build();
//
//        final Collection<JsonObject> expandedProofs = EmbeddedProof.expandedProofs(data.expanded());
//
//        return new Vcdm(
//                version,
//                new GenericMaterial(
//                        data.context(),
//                        compactedUnsigned,
//                        expandedUnsigned),
//                compactedProofs != null
//                        ? compactedProofs
//                        : Collections.emptyList(),
//                expandedProofs != null
//                        ? expandedProofs
//                        : Collections.emptyList());
    }

//    protected abstract VcdmVersion modelVersion(Collection<String> context) throws DocumentError;
}
