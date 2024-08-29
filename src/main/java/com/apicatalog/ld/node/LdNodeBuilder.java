package com.apicatalog.ld.node;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.ld.Term;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

@Deprecated
public class LdNodeBuilder {

    final JsonObjectBuilder builder;

    public LdNodeBuilder() {
        this(Json.createObjectBuilder());
    }

    public LdNodeBuilder(final JsonObjectBuilder builder) {
        this.builder = builder;
    }
    
    public LdNodeBuilder(final JsonObject object) {
        this.builder = Json.createObjectBuilder(object);
    }
    
    public static LdNodeBuilder of(final JsonObject object) {
        return new LdNodeBuilder(object);
    }
    
    public LdNodeBuilder type(String type) {
        builder.add(Keywords.TYPE, Json.createArrayBuilder().add(type));
        return this;
    }
    
    public LdSetter set(Term term) {
        return new LdSetter(this, term, builder, null);
    }
    
    public JsonObject build() {
        return builder.build();
    }

    public void id(URI id) {
        builder.add(Keywords.ID, id.toString());
    }

    public void type(Collection<String> type) {
        builder.add(Keywords.TYPE, Json.createArrayBuilder(type));        
    }
}
