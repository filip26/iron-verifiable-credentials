package com.apicatalog.vc.jsonld;

import java.util.Collection;

import jakarta.json.JsonObject;

@FunctionalInterface
public interface JsonLdContextAdapter {

    Collection<String> context(JsonObject document);
    
}
