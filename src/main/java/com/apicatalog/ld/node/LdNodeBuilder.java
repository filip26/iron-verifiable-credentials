package com.apicatalog.ld.node;

import java.net.URI;

import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.schema.LdTerm;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

public class LdNodeBuilder {

    final JsonObjectBuilder builder;

    public LdNodeBuilder() {
        this(Json.createObjectBuilder());
    }

    public LdNodeBuilder(final JsonObjectBuilder builder) {
        this.builder = builder;
    }
    
    public LdNodeBuilder type(String type) {
        builder.add(Keywords.TYPE, Json.createArrayBuilder().add(type));
        return this;
    }
    
    public LdSetter set(LdTerm term) {
        return new LdSetter(term, builder, null);
    }
    
    public JsonObject build() {
        return builder.build();
    }

    public void id(URI id) {
        builder.add(Keywords.ID, id.toASCIIString());
    }
}
