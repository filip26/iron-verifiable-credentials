package com.apicatalog.ld.node;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public class LdTypeGetter {

    final JsonObject object;
    
    boolean required;
    
    public LdTypeGetter(JsonObject object) {
        this.object = object;
        this.required = false;
    }
    
    public LdTypeGetter required() {
        this.required = true;
        return this;
    }
    
    public URI link() throws DocumentError {
        final String link = string();
        
        return link != null ? URI.create(link) : null;
    }
    
    public String string() throws DocumentError {
        JsonValue type = object.get(Keywords.TYPE);

        if (JsonUtils.isNotNull(type)) {
            Collection<JsonValue> types = JsonUtils.toCollection(type);
            
            if (types.size() > 1) {
                throw new DocumentError(ErrorType.Invalid, Keywords.TYPE);
            }
            if (types.size() > 0) {
                type = types.iterator().next();
            }
        }
        
        if (JsonUtils.isNull(type)) {
            if (required) {
                throw new DocumentError(ErrorType.Missing, Keywords.TYPE);
            }
            return null;            
        }
        
        if (JsonUtils.isString(type)) {
            return ((JsonString)type).getString();
        }

        throw new DocumentError(ErrorType.Invalid, Keywords.TYPE);
    }
    
    
}
