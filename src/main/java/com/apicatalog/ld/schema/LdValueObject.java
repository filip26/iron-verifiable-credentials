package com.apicatalog.ld.schema;

import java.util.function.Function;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class LdValueObject implements LdValueAdapter<JsonValue, JsonValue> {

    protected final LdTerm type;
    
    public LdValueObject() {
        this(null);
    }
    
    public LdValueObject(LdTerm type) {
        this.type = type;
    }
    
    @Override
    public JsonValue read(JsonValue value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JsonValue write(JsonValue value) {
        // TODO Auto-generated method stub
        return null;
    }

}
