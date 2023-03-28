package com.apicatalog.jsonld.schema.adapter;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.schema.ParametrizedPredicate;
import com.apicatalog.ld.DocumentError;

import jakarta.json.JsonValue;

public class ArrayFilter<T> implements LdValueAdapter<JsonValue, T> {

    protected final LdValueAdapter<JsonValue, T> adapter;
    protected final ParametrizedPredicate<T> test;
    
    public ArrayFilter(LdValueAdapter<JsonValue, T> adapter, ParametrizedPredicate<T> test) {
        this.adapter = adapter;
        this.test = test;
    }

    @Override
    public T read(JsonValue array) throws DocumentError {

        if (JsonUtils.isNull(array)) {
            return null;
        }
        
        for (final JsonValue value : JsonUtils.toJsonArray(array)) {
            
            T result = adapter.read(value);
            
            if (test.test(result, null)) {
                return result;
            }
        }
        return null;
    }

    @Override
    public JsonValue write(T value) throws DocumentError {

        if (value == null) {
            return JsonValue.NULL;
        }
        
        return adapter.write(value);       
    }
}
