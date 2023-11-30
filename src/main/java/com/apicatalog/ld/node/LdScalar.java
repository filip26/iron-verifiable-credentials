package com.apicatalog.ld.node;

import java.net.URI;
import java.time.Instant;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.schema.LdTerm;
import com.apicatalog.jsonld.schema.adapter.XsdDateTimeAdapter;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.multibase.Multibase;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public class LdScalar {

    static final String XSD_DATE_TIME = "http://www.w3.org/2001/XMLSchema#dateTime";

    final LdTerm term;
    final JsonObject value;

    public LdScalar(LdTerm term, JsonObject value) {
        this.term = term;
        this.value = value;
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
        if (JsonUtils.isNotNull(string)) {
            throw new DocumentError(ErrorType.Invalid, term);
        }

        return null;
    }

    protected JsonValue value() throws DocumentError {

        JsonValue jsonValue = null;

        if (value != null) {
            jsonValue = value.get(Keywords.VALUE);
        }

        if (JsonUtils.isNull(jsonValue)) {
            return null;
        }

        return jsonValue;
    }

    public boolean exists() {
        return JsonUtils.isNotNull(value);
    }

    public LdTypeGetter type() {
        return new LdTypeGetter(value);
    }

    public byte[] multibase(Multibase base) throws DocumentError {

        String string = string();

        if (string != null) {
            return base.decode(string);
        }
        return null;
    }

    public Instant xsdDateTime() throws DocumentError {

        if (value == null) {
            return null;
        }

        if (type().exists() && !type().hasType(XSD_DATE_TIME)) {
            throw new DocumentError(ErrorType.Invalid, term);
        }

        String string = string();
        if (string != null) {
            try {
                return (new XsdDateTimeAdapter()).read(string);
            } catch (IllegalArgumentException e) {
                throw new DocumentError(e, ErrorType.Invalid, term);
            }
        }
        return null;
    }
}
