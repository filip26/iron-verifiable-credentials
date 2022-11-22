package com.apicatalog.jsonld;

import com.apicatalog.jsonld.lang.Keywords;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class JsonLdValueObject {

    public static final JsonObject create(String type, String value) {
        return Json.createObjectBuilder().add(Keywords.TYPE, type).add(Keywords.VALUE, value)
                .build();
    }

    public static final JsonValue toJson(String type, String value) {
        return Json.createArrayBuilder().add(create(type, value)).build();
    }

    public static final JsonValue toJson(String value) {
        return Json.createObjectBuilder().add(Keywords.VALUE, value).build();
    }
}
