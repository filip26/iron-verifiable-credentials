package com.apicatalog.vc.verifier;

import com.apicatalog.vc.Verifiable;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

/**
 * Provides various representation of the same verifiable material. Those
 * representation must be semantically equivalent.
 */
public record VerifiableMaterial(
        JsonObject compacted,
        JsonArray expanded,
        Verifiable materialized
        ) {
    
}
