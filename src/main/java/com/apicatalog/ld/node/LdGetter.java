package com.apicatalog.ld.node;

import java.net.URI;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.lang.ValueObject;
import com.apicatalog.jsonld.schema.LdTerm;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public class LdGetter {

    final LdTerm term;
    final JsonValue value;

    protected LdGetter(LdTerm term, JsonValue value) {
        this.term = term;
        this.value = value;
    }

    public LdNode node() throws DocumentError {
        return new LdNode(object());
    }

    public JsonObject object() throws DocumentError {

        JsonValue object = null;

        if (JsonUtils.isNotNull(value)) {

            JsonArray values = JsonUtils.toJsonArray(value);

            if (values.size() > 1) {
                throw new DocumentError(ErrorType.Invalid, term);
            }
            if (values.size() > 0) {
                object = values.get(0);
            }
        }

        if (JsonUtils.isObject(object)) {
            return object.asJsonObject();
        }

        if (JsonUtils.isNotNull(object)) {
            throw new DocumentError(ErrorType.Invalid, term);
        }
        return null;
    }

    public LdScalar scalar() throws DocumentError {

        JsonValue scalar = null;

        if (JsonUtils.isNotNull(value)) {

            JsonArray values = JsonUtils.toJsonArray(value);

            if (values.size() > 1) {
                throw new DocumentError(ErrorType.Invalid, term);
            }
            if (values.size() > 0) {
                scalar = values.get(0);
            }
        }

        if (ValueObject.isValueObject(scalar)) {
            return new LdScalar(term, scalar.asJsonObject());
        }

        if (JsonUtils.isNotNull(scalar)) {
            throw new DocumentError(ErrorType.Invalid, term);
        }
        
        return new LdScalar(term, null);
    }

    public URI id() throws DocumentError {

        JsonValue scalar = null;

        if (JsonUtils.isNotNull(value)) {

            JsonArray values = JsonUtils.toJsonArray(value);

            if (values.size() > 1) {
                throw new DocumentError(ErrorType.Invalid, term);
            }
            if (values.size() > 0) {
                scalar = values.get(0);
            }
        }

        if (JsonUtils.isString(scalar)) {
            return URI.create(((JsonString) scalar).getString());
        }

        if (JsonUtils.containsKey(scalar, Keywords.ID)) {
            JsonValue id = scalar.asJsonObject().get(Keywords.ID);
            if (JsonUtils.isString(id)) {
                return URI.create(((JsonString) id).getString());
            }
        }

        if (JsonUtils.isNotNull(scalar)) {
            throw new DocumentError(ErrorType.Invalid, term);
        }

        return null;
    }

    public boolean exists() {
        return JsonUtils.isNotNull(value);
    }
}
