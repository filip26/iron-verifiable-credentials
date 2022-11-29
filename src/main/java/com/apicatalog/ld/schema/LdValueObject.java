package com.apicatalog.ld.schema;

import java.util.function.Function;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class LdValueObject implements LdValue<JsonValue, JsonValue> {

    protected final LdTerm type;
    
    public LdValueObject() {
        this(null);
    }
    
    public LdValueObject(LdTerm type) {
        this.type = type;
    }
    
    @Override
    public JsonValue apply(JsonValue value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JsonValue inverse(JsonValue value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <X> LdValue<JsonValue, X> map(LdValueAdapter<JsonValue, X> adapter) {
        // TODO Auto-generated method stub
        return null;
    }


}
