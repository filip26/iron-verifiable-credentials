package com.apicatalog.ld.node;

import java.net.URI;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.schema.LdTerm;
import com.apicatalog.ld.DocumentError;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public class LdNode {

    final JsonObject object;
    
    public LdNode(final JsonObject object) {
        this.object = object;
    }
    
    public URI id() {
        JsonValue id = object.get(Keywords.ID);
        
        if (JsonUtils.isString(id)) {
            return URI.create(((JsonString)id).getString());
        }
        
        return null;
    }
    
    public LdGetter get(LdTerm term) {
        return get(object, term);
    }    
    
    public static LdGetter get(JsonObject object, LdTerm term) {
        return new LdGetter(term, object.get(term.uri()));
    }

    public <T> T map(LdAdapter<T> adapter) throws DocumentError {
        return adapter.read(object);
    }

    public LdTypeGetter type() {        
        return new LdTypeGetter(object);
    }

}
