package com.apicatalog.ld.node;

import java.util.Map;

import com.apicatalog.ld.DocumentError;

import jakarta.json.JsonObject;

//@FunctionalInterface
public interface LdAdapter<T> {

    T read(JsonObject value) throws DocumentError;
    
    JsonObject write(T value);
//        boolean test(T value, Map<String, Object> params);
    
}
