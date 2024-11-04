package com.apicatalog.vc.verifier;

import java.util.Collection;

import jakarta.json.JsonObject;

/**
 * Provides various representation of the same verifiable material. Those
 * representation must be semantically equivalent.
 */
public record VerifiableMaterial(
        Collection<String> context,
        JsonObject compacted,
        JsonObject expanded
        ) {

}
