package com.apicatalog.ld.adapter;

import com.apicatalog.ld.DocumentError;

import jakarta.json.JsonObject;

public interface LdAdapter<T> {

    T read(JsonObject value) throws DocumentError;
    
    JsonObject write(T value);
    
}
