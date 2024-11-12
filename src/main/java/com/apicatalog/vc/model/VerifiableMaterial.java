package com.apicatalog.vc.model;

import java.util.Collection;

import jakarta.json.JsonObject;

/**
 * Provides various representation of the same verifiable material. Those
 * representation must be semantically equivalent.
 */
public interface VerifiableMaterial {
    
    Collection<String> context();

    JsonObject compacted();

    JsonObject expanded();
    
}
