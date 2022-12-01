package com.apicatalog.ld.schema.adapter;

import java.net.URI;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.ld.schema.LdValueAdapter;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public class LinkAdapter implements LdValueAdapter<JsonValue, URI> {

    
    @Override
    public URI read(JsonValue json) {

        if (JsonUtils.isNotObject(json)) {
            throw new IllegalArgumentException();
        }
        
        JsonObject object = json.asJsonObject();
        
            
        JsonValue value = object.get(Keywords.ID);

        // unwrap 
        if (JsonUtils.isArray(value) && value.asJsonArray().size() == 1) {
            value = value.asJsonArray().get(0);
        }
        
        if (JsonUtils.isNotString(value)) {
            throw new IllegalArgumentException();
        }
            
        return URI.create(((JsonString)value).getString());
    }

    @Override
    public JsonObject write(URI link) {
        
        JsonObjectBuilder builder = Json.createObjectBuilder();
        

        builder.add(Keywords.ID, link.toString());
        
        return builder.build();
    }     
}
