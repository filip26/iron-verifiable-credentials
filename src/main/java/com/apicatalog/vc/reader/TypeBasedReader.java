package com.apicatalog.vc.reader;

import java.util.Map;

import com.apicatalog.vcdm.VcdmVersion;

import jakarta.json.JsonObject;

public class TypeBasedReader<T> implements ObjectReader<JsonObject, T> {

    protected Map<String, ObjectReader<JsonObject, T>> mapping;
    
    protected TypeBasedReader(Map<String, ObjectReader<JsonObject, T>> mapping) {
        this.mapping = mapping;
    }
        
    @Override
    public T read(VcdmVersion version, JsonObject object) {
    
        //TODO
        
        return null;
    }

}
