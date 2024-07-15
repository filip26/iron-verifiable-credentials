package com.apicatalog.vc.status.reader;

import java.util.Map;

import com.apicatalog.vc.ModelVersion;
import com.apicatalog.vc.reader.ExpandedObjectReader;

import jakarta.json.JsonObject;

public class TypeBasedReader<T> implements ExpandedObjectReader<T> {

    protected Map<String, ExpandedObjectReader<T>> mapping;
    
    protected TypeBasedReader(Map<String, ExpandedObjectReader<T>> mapping) {
        this.mapping = mapping;
    }
        
    @Override
    public T read(ModelVersion version, JsonObject object) {
    
        //TODO
        
        return null;
    }

}
