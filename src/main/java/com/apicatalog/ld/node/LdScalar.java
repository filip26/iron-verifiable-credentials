package com.apicatalog.ld.node;

import java.net.URI;
import java.time.Instant;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.schema.LdTerm;
import com.apicatalog.jsonld.schema.adapter.LdValueAdapter;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public class LdScalar {

    final LdTerm term;
    final JsonObject value;

    boolean required;

    public LdScalar(LdTerm term, JsonObject value, boolean required) {
        this.term = term;
        this.value = value;
        this.required = required;
    }

    public LdScalar required() {
        this.required = true;
        return this;
    }

    public <T> T map(LdValueAdapter<String, T> adapter) {
        // TODO Auto-generated method stub
        return null;
    }

    public Instant xsdDateTime() {
        // TODO check @type map(new XsdDateTimeAdapter());
        return null;
    }

    public URI link() throws DocumentError {

        String link = string();

        return link != null ? URI.create(link) : null;
    }

    public String string() throws DocumentError {

        JsonValue string = value();

        if (JsonUtils.isString(string)) {
            return ((JsonString) string).getString();
        }

        throw new DocumentError(ErrorType.Invalid, term);
    }

    protected JsonValue value() throws DocumentError {

        JsonValue jsonValue = null;

        if (value != null) {
            jsonValue = value.get(Keywords.VALUE);
        }

        if (JsonUtils.isNull(jsonValue)) {
            if (required) {
                throw new DocumentError(ErrorType.Missing, term);
            }
            return null;
        }

        return jsonValue;
    }

    public byte[] multibase() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean exists() {
        return JsonUtils.isNotNull(value);
    }

    public LdTypeGetter type() {
        return new LdTypeGetter(value);
    }
}
