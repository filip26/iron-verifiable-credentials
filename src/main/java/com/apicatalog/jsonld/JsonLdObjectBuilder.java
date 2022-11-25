package com.apicatalog.jsonld;

import java.net.URI;
import java.time.Instant;

import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.uri.UriUtils;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

public class JsonLdObjectBuilder {

    static final String XSD_DATE_TIME = "http://www.w3.org/2001/XMLSchema#dateTime";

    protected final JsonObjectBuilder builder;
    
    protected String vocab;
    
    public JsonLdObjectBuilder() {
        this(Json.createObjectBuilder());
    }

    public JsonLdObjectBuilder(JsonObject object) {
        this(Json.createObjectBuilder(object));
    }

    public JsonLdObjectBuilder(JsonObjectBuilder builder) {
        this.builder = builder;
    }

    public JsonLdObjectBuilder vocab(String vocab) {
        this.vocab = vocab;
        return this;
    }
    
    public JsonLdObjectBuilder add(String name, String value) {
        setValue(expandNameOrFail(vocab, name), value);
        return this;
    }

    public JsonLdObjectBuilder add(String name, String type, String value) {
        setValue(expandNameOrFail(vocab, name), value);
        return this;
    }

    public JsonLdObjectBuilder add(String name, Instant value) {
        setValue(expandNameOrFail(vocab, name), value);
        return this;
    }
    
    public void setId(String property, URI id) {
        setId(property, id.toString());
    }

    public void setId(String property, String id) {
        builder.add(property, Json.createArrayBuilder().add(Json.createObjectBuilder().add(Keywords.ID, id)));
    }
    
    void setValue(String property, String type, String value) {
        builder.add(property, JsonLdValueObject.toJson(type, value));
    }

    void setValue(String property, String value) { 
        builder.add(property, JsonLdValueObject.toJson(value));
    }

    void setValue(final String property, Instant instant) {
        setValue(property, XSD_DATE_TIME, instant.toString());
    }
    
    protected static String expandNameOrFail(String vocab, String name) {
        if (UriUtils.isAbsoluteUri(name, true)) {
            return name;
        }
        if (vocab == null) {
            throw new IllegalArgumentException("The paramenter 'name' must be an absolute URI or vocabulary must be defined (look at vocab() method) .");
        }
        return vocab.concat(name);
    }

    public JsonLdObjectBuilder setType(URI type) {
        builder.add(Keywords.TYPE, Json.createArrayBuilder().add(type.toString()));
        return this;
    }
    //TODO addType
    
    public JsonObject build() {
        return builder.build();
    }
}
