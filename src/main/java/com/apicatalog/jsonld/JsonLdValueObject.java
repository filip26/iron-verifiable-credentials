package com.apicatalog.jsonld;

import com.apicatalog.jsonld.lang.Keywords;

import jakarta.json.Json;
import jakarta.json.JsonObject;

//TODO use Titanium 1.3.1
public class JsonLdValueObject {

    public static final JsonObject create(String type, String value) {
        return Json.createObjectBuilder()
                .add(Keywords.TYPE, type)
                .add(Keywords.VALUE, value)
                .build();
    }
}
