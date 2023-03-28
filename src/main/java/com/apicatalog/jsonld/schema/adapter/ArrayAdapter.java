package com.apicatalog.jsonld.schema.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.ld.DocumentError;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonValue;

public class ArrayAdapter<T> implements LdValueAdapter<JsonValue, Collection<T>> {

    protected final LdValueAdapter<JsonValue, T> adapter;
    
    public ArrayAdapter(LdValueAdapter<JsonValue, T> adapter) {
        this.adapter = adapter;
    }

    @Override
    public Collection<T> read(JsonValue array) throws DocumentError {

        if (JsonUtils.isNull(array)) {
            return null;
        }
        
        final Collection<T> result = new ArrayList<>();
        
        for (final JsonValue value : JsonUtils.toJsonArray(array)) {
            result.add(adapter.read(value));
        }
        
        return result;
    }

    @Override
    public JsonValue write(Collection<T> array) throws DocumentError {

        if (array == null) {
            return JsonValue.NULL;
        }
        
        final JsonArrayBuilder result = Json.createArrayBuilder();
        
        for (T value : array) {
            result.add(adapter.write(value));
        }
        
        return result.build();
    }

    public LdValueAdapter<JsonValue, T> find(Predicate<T> test) {
        return new ArrayFilter<T>(adapter, (v, p) -> test.test(v));
    }    

}
