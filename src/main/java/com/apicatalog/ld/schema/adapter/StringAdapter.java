package com.apicatalog.ld.schema.adapter;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.ld.schema.LdValueAdapter;

import jakarta.json.Json;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public class StringAdapter implements LdValueAdapter<JsonValue, String> {

    @Override
    public String read(JsonValue value) {

        if (JsonUtils.isNull(value)) {
            return null;
        }
        
        if (JsonUtils.isString(value)) {
            return ((JsonString)value).getString();
        }
        
        throw new IllegalArgumentException("Expected JsonString but have [" + value.getValueType() + "]");
    }

    @Override
    public JsonValue write(String value) {
        return Json.createValue(value);
    }


}
