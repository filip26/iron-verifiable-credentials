package com.apicatalog.ld.schema.adapter;

import java.util.function.Function;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.lang.ValueObject;
import com.apicatalog.ld.schema.LdTerm;
import com.apicatalog.ld.schema.LdValueAdapter;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class LdValueObjectAdapter implements LdValueAdapter<JsonValue, JsonValue> {

    protected final LdTerm type;
    
    public LdValueObjectAdapter() {
        this(null);
    }
    
    public LdValueObjectAdapter(LdTerm type) {
        this.type = type;
    }
    
    @Override
    public JsonValue read(JsonValue value) {
        
        if (!ValueObject.isValueObject(value)) {
            throw new IllegalArgumentException();
        }
        
        //TODO check type
        
        return value.asJsonObject().get(Keywords.VALUE);
    }

    @Override
    public JsonValue write(JsonValue value) {
        // TODO Auto-generated method stub
        return null;
    }

}
