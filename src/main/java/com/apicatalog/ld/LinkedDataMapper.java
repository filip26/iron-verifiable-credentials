package com.apicatalog.ld;

import java.util.Map;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public class LinkedDataMapper {
    
    protected Map<String, LdMapping> mappings;

    public LdValue deserialize(final JsonValue value) {

        if (JsonUtils.isNull(value)) {
            //TODO
            return null;
            
        } else if (JsonUtils.isArray(value)) {
            
            if (value.asJsonArray().size() == 0) {
                //TODO
                return null;
            }
            
            if (value.asJsonArray().size() == 1) {
                return deserialize(value.asJsonArray().get(0));                
            }
            
            //TODO
            
        } else if (JsonUtils.isObject(value)) {
            
            final JsonObject subject = value.asJsonObject();
            
            if (subject.containsKey(Keywords.GRAPH) &&  subject.size() == 1) {
                return deserialize(subject.get(Keywords.GRAPH));
            }
            
            if (subject.containsKey(Keywords.TYPE)) {
                
                for (final JsonValue type : JsonUtils.toCollection(subject.get(Keywords.TYPE))) {
                    
                    if (JsonUtils.isString(type)) {
                        final LdMapping mapping = mappings.get(((JsonString)type).getString());
                        if (mapping != null) {
                            return mapping.deserialize(subject);
                        }
                    }                    
                }
            }
            
            if (subject.containsKey(Keywords.VALUE)) {
                
            }
            

            //TODO check @type and if missing then @id, then @value 

        } else if (JsonUtils.isScalar(value)) {
            //TODO this should not happen, but ...
        }

        throw new IllegalStateException();
    }
    
    public JsonValue serialize(LdValue value) {
        //TODO
        return null;
    }
    
}
