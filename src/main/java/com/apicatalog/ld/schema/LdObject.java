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
        System.out.println(">>> " + values);
        System.out.println(">>> " + term.id);

        return values.containsKey(term.id);
    }

    public <X> X value(LdTerm term) {
        System.out.println("VALUE " + term.name + ", " + values);
        return (X) values.get(term.id);
    }

    
     
}
