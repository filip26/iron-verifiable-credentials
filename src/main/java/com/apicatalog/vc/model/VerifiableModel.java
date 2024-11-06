package com.apicatalog.vc.model;

import java.util.Collection;

import jakarta.json.JsonObject;

public interface VerifiableModel {

    VerifiableMaterial data();

    Collection<JsonObject> compactedProofs();
    
    Collection<JsonObject> expandedProofs();
}
