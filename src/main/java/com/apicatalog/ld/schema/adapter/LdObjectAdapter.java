package com.apicatalog.ld.schema.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.apicatalog.ld.schema.LdObject;
import com.apicatalog.ld.schema.LdProperty;
import com.apicatalog.ld.schema.LdTag;
import com.apicatalog.ld.schema.LdTerm;
import com.apicatalog.ld.schema.LdValueAdapter;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class LdObjectAdapter implements LdValueAdapter<JsonObject, LdObject> {

    protected final Map<String, LdProperty<?>> terms;
    protected final Map<LdTag, LdProperty<?>> tags;
    
    protected LdObjectAdapter(Map<String, LdProperty<?>> terms, Map<LdTag, LdProperty<?>> tags) {
        this.terms = terms;
        this.tags = tags;
    }
    
    @Override
    public LdObject read(JsonObject object) {
        
        final Map<String, Object> values = new LinkedHashMap<>(object.size());
        
        for (final Map.Entry<String, JsonValue> entry : object.entrySet()) {
            
            // ignore if undefined
            if (!terms.containsKey(entry.getKey())) {
                continue;
            }
            
            JsonValue value = entry.getValue();
            
            LdProperty<?> property = terms.get(entry.getKey());
            
            values.put(entry.getKey(), property.read(value));
        }
        
        return new LdObject(Collections.unmodifiableMap(values));
    }

    @Override
    public JsonObject write(LdObject value) {
        
        
        // TODO Auto-generated method stub
        return null;
    }


    public static LdObjectAdapter create(LdProperty<?>[] properties) {
        
        final Map<String, LdProperty<?>> terms = new LinkedHashMap<>(properties.length);
        final Map<LdTag, LdProperty<?>> tags = new LinkedHashMap<>(LdTag.values().length);
        
        for (LdProperty<?> property : properties) {
            if (property.tag() != null) {
                tags.put(property.tag(), property);
            }
            terms.put(property.term().id(), property);
        }
        
        return new LdObjectAdapter(
                        Collections.unmodifiableMap(terms),
                        Collections.unmodifiableMap(tags)
                        );
    }

    @SuppressWarnings("unchecked")
    public <X> LdProperty<X> property(LdTag tag) {
        return (LdProperty<X>) tags.get(tag);
    }

    public boolean contains(LdTerm term) {
        return terms.containsKey(term);
    }
     
}
