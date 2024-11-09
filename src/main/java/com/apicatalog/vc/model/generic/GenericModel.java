package com.apicatalog.vc.model.generic;

import java.util.Collection;

import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.model.VerifiableModel;

import jakarta.json.JsonObject;

public record GenericModel(
        VerifiableMaterial data,
        Collection<JsonObject> compactedProofs,
        Collection<JsonObject> expandedProofs) implements VerifiableModel {
}
