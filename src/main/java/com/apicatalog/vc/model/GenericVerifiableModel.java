package com.apicatalog.vc.model;

import java.util.Collection;

import jakarta.json.JsonObject;

public record GenericVerifiableModel(
        VerifiableMaterial data,
        Collection<JsonObject> compactedProofs,
        Collection<JsonObject> expandedProofs) implements VerifiableModel {
}
