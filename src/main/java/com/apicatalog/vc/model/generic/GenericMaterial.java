package com.apicatalog.vc.model.generic;

import java.util.Collection;

import com.apicatalog.vc.model.VerifiableMaterial;

import jakarta.json.JsonObject;

public record GenericMaterial(
        Collection<String> context,
        JsonObject compacted,
        JsonObject expanded) implements VerifiableMaterial {

}
