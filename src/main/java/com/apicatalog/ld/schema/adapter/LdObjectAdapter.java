package com.apicatalog.ld.schema.adapter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.schema.LdObject;
import com.apicatalog.ld.schema.LdProperty;
import com.apicatalog.ld.schema.LdTerm;
import com.apicatalog.ld.schema.LdValueAdapter;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;

public class LdObjectAdapter implements LdValueAdapter<JsonValue, LdObject> {

    protected final Map<String, LdProperty<Object>> terms;
    protected final Map<String, LdProperty<Object>> tags;
    
    protected LdObjectAdapter(Map<String, LdProperty<Object>> terms, Map<String, LdProperty<Object>> tags) {
        this.terms = terms;
        this.tags = tags;
    }
    
    @Override
    public LdObject read(JsonValue json) {

        if (JsonUtils.isNotObject(json)) {
            throw new IllegalArgumentException();
        }
        
        JsonObject object = json.asJsonObject();
        

        final Map<String, Object> values = new LinkedHashMap<>(object.size());
        
        for (final Map.Entry<String, JsonValue> entry : object.entrySet()) {
            
            // ignore if undefined
            if (!terms.containsKey(entry.getKey())) {
                continue;
            }

            JsonValue jsonValue = entry.getValue();

            // unwrap 
            if (JsonUtils.isArray(jsonValue) && jsonValue.asJsonArray().size() == 1) {
                jsonValue = jsonValue.asJsonArray().get(0);
            }
            
            LdProperty<Object> property = terms.get(entry.getKey());
            
            if (JsonUtils.isNull(jsonValue)) {
                continue;
            }
            
            values.put(entry.getKey(), property.read(jsonValue));
        }
        
        return new LdObject(Collections.unmodifiableMap(values));
    }

    @Override
    public JsonObject write(LdObject object) {
        
        JsonObjectBuilder builder = Json.createObjectBuilder();

        for (final Map.Entry<String, Object> entry : object.entrySet()) {

            //TODO defaultValue?
            if (entry.getValue() == null) {
                continue;
            }
            
            // ignore if undefined
            if (!terms.containsKey(entry.getKey())) {
                continue;
            }

            @SuppressWarnings("unchecked")
            LdProperty<Object> property = (LdProperty<Object>) terms.get(entry.getKey());
            
            JsonValue value =  property.write(entry.getValue());
            
            // wrap
            if (!Keywords.ID.equalsIgnoreCase(entry.getKey())) {
                value = Json.createArrayBuilder().add(value).build();
            }

            builder.add(entry.getKey(), value);
        }        
        
        return builder.build();
    }

    public static LdObjectAdapter create(LdProperty<?>[] properties) {
        
        final Map<String, LdProperty<Object>> terms = new LinkedHashMap<>(properties.length);
        final Map<String, LdProperty<Object>> tags = new LinkedHashMap<>(5);
        
        for (LdProperty<?> property : properties) {
            if (property.tag() != null) {
                tags.put(property.tag(), (LdProperty<Object>) property);
            }
            terms.put(property.term().id(), (LdProperty<Object>) property);
        }
        
        return new LdObjectAdapter(
                        Collections.unmodifiableMap(terms),
                        Collections.unmodifiableMap(tags)
                        );
    }

    @SuppressWarnings("unchecked")
    public <X> LdProperty<X> property(String tag) {
        return (LdProperty<X>) tags.get(tag);
    }

    public boolean contains(LdTerm term) {
        return terms.containsKey(term.id());
    }

    public void validate(LdObject object, Map<String, Object> params) throws DocumentError {

        for (final LdProperty<Object> property : terms.values()) {

            Object value = null;
            
            if (object.contains(property.term())) {
                value = object.value(property.term());
            }
            

                property.validate(value, params);
        }
    }
     
}
