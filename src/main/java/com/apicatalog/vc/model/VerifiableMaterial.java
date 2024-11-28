package com.apicatalog.vc.model;

import com.apicatalog.linkedtree.jsonld.JsonLdContext;

import jakarta.json.JsonObject;

/**
 * Provides various representation of the same verifiable material. Those
 * representation must be semantically equivalent.
 * 
 * @implNote Will be generalized in the next version. It's hardwired to
 *           JSON-LD, now.
 */
public interface VerifiableMaterial {

    JsonLdContext context();

    JsonObject compacted();

    JsonObject expanded();

}
