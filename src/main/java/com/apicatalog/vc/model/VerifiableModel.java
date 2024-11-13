package com.apicatalog.vc.model;

import java.util.Collection;

import com.apicatalog.ld.DocumentError;

import jakarta.json.JsonObject;

public interface VerifiableModel {

    VerifiableMaterial data();

    Collection<JsonObject> compactedProofs();

    Collection<JsonObject> expandedProofs();

    VerifiableMaterial materialize() throws DocumentError;

    VerifiableModel withProof(VerifiableMaterial signedProof);

//    JsonObject compacted();

    // TODO ? Collection<VerifiableMaterial> proofs();
}
