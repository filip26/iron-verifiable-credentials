package com.apicatalog.vcdm;

import java.util.Collection;

import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.model.VerifiableModel;

import jakarta.json.JsonObject;

public record Vcdm(
        VcdmVersion version,
        VerifiableMaterial data,
        Collection<JsonObject> compactedProofs,
        Collection<JsonObject> expandedProofs
        ) implements VerifiableModel {

}
