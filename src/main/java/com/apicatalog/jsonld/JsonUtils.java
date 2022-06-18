package com.apicatalog.jsonld;

import java.util.Optional;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class JsonUtils {

    public static Optional<JsonObject> findFirstObject(JsonValue expanded) {
        if (com.apicatalog.jsonld.json.JsonUtils.isArray(expanded)) {
            for (JsonValue item : expanded.asJsonArray()) {
                if (com.apicatalog.jsonld.json.JsonUtils.isObject(item)) {
                    return Optional.of(item.asJsonObject());
                }
            }
        } else if (com.apicatalog.jsonld.json.JsonUtils.isObject(expanded)) {
            return Optional.of(expanded.asJsonObject());
        }

        return Optional.empty();
    }


}
