package com.apicatalog.ld.schema;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
    
    public Set<Entry<String, Object>> entrySet() {
        return values.entrySet();
    }

    
     
}
