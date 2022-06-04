package com.apicatalog.lds.proof;

import java.net.URI;

import com.apicatalog.jsonld.lang.Keywords;

import jakarta.json.Json;
import jakarta.json.JsonObject;

public class VerificationMethodReference implements VerificationMethod {

    private final URI id;
    
    public VerificationMethodReference(URI id) {
        this.id = id;
    }

    @Override
    public URI getId() {
        return id;
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public URI getController() {
        return null;
    }

    @Override
    public JsonObject toJson() {

        return Json.createObjectBuilder().add(Keywords.ID, id.toString()).build();
    }

}
