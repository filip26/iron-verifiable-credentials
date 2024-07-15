package com.apicatalog.vc.status.reader;

import java.util.Map;

import com.apicatalog.vc.status.Status;

import jakarta.json.JsonObject;

public class TypeBasedStatusReader implements StatusReader {

    protected Map<String, StatusReader> mapping;
    
    protected TypeBasedStatusReader(Map<String, StatusReader> mapping) {
        this.mapping = mapping;
    }
        
    @Override
    public Status read(JsonObject object) {
    
        //TODO
        
        return null;
    }

}
