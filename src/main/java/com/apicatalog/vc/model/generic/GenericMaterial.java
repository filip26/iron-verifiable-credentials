package com.apicatalog.vc.model.generic;

import com.apicatalog.linkedtree.jsonld.JsonLdContext;
import com.apicatalog.vc.model.VerifiableMaterial;

import jakarta.json.JsonObject;

public record GenericMaterial(
        JsonLdContext context,
        JsonObject compacted,
        JsonObject expanded) implements VerifiableMaterial {

}
