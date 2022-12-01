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

        return values.containsKey(term.id);
    }

    @SuppressWarnings("unchecked")
    public <X> X value(LdTerm term) {
        return (X) values.get(term.id);
    }
    
    public Set<Entry<String, Object>> entrySet() {
        return values.entrySet();
    }

    
     
}
