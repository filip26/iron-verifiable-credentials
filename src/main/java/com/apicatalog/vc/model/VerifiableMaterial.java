package com.apicatalog.vc.model;

import com.apicatalog.linkedtree.jsonld.JsonLdContext;

import jakarta.json.JsonObject;

/**
 * Provides various representation of the same verifiable material. Those
 * representation must be semantically equivalent.
 */
public interface VerifiableMaterial {
    
    JsonLdContext context();

    JsonObject compacted();

    JsonObject expanded();
    
}
