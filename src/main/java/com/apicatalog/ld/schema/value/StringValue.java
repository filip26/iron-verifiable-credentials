package com.apicatalog.ld.schema.value;

import java.util.function.Function;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.ld.schema.LdValue;
import com.apicatalog.ld.schema.LdValueAdapter;

import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public class StringValue implements LdValue<JsonValue, String> {

    @Override
    public String apply(JsonValue value) {

        if (JsonUtils.isString(value)) {
            return ((JsonString)value).getString();
        }
        
        throw new IllegalArgumentException();
    }

    @Override
    public JsonValue inverse(String value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <X> LdValue<JsonValue, X> map(LdValueAdapter<String, X> adapter) {
        // TODO Auto-generated method stub
        return null;
    }

}
