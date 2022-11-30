package com.apicatalog.ld.schema;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.json.JsonObject;

public class LdObject  {

    final Map<String, Object> values;
    
    public LdObject(Map<String, Object> values) {
        this.values = values;
    }
    
    public boolean contains(LdTerm term) {
        return values.containsKey(term.id);
    }

    public <X> X value(LdTerm term) {
        return (X) values.get(term.id);
    }

    
     
}
