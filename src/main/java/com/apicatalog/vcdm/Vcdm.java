package com.apicatalog.vcdm;

import java.util.ArrayList;
import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.model.VerifiableModel;
import com.apicatalog.vc.model.VerifiableModelWriter;

import jakarta.json.JsonObject;

public record Vcdm(
        VcdmVersion version,
        VerifiableMaterial data,
//        Collection<VerifiableMaterial> proofs
        Collection<JsonObject> compactedProofs,
        Collection<JsonObject> expandedProofs,
        VerifiableModelWriter writer) implements VerifiableModel {

    @Override
    public VerifiableMaterial materialize() throws DocumentError {
        return writer.write(this);
    }

    @Override
    public VerifiableModel withProof(VerifiableMaterial signedProof) {

        Collection<JsonObject> expanded = new ArrayList<>(expandedProofs);
        expanded.add(signedProof.expanded());

        Collection<JsonObject> compacted = new ArrayList<>(compactedProofs);
        compacted.add(signedProof.compacted());

        return new Vcdm(
                version,
                data,
                compacted,
                expanded,
                writer);
    }

}
