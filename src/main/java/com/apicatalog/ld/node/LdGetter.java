package com.apicatalog.ld.node;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.ValueObject;
import com.apicatalog.jsonld.schema.LdTerm;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class LdGetter {

    final LdTerm term;
    final JsonValue value;

    boolean required = false;

    protected LdGetter(LdTerm term, JsonValue value) {
        this.term = term;
        this.value = value;
    }

    public LdGetter required() {
        this.required = true;
        return this;
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

            object = values.get(0);
        }

        if (JsonUtils.isObject(object)) {
            return object.asJsonObject();
        }

        if (JsonUtils.isNull(object)) {
            if (required) {
                throw new DocumentError(ErrorType.Missing, term);
            }
            return null;
        }

        throw new DocumentError(ErrorType.Invalid, term);        
    }

    public LdScalar scalar() throws DocumentError {

        JsonValue scalar = null;

        if (JsonUtils.isNotNull(value)) {

            JsonArray values = JsonUtils.toJsonArray(value);

            if (values.size() > 1) {
                throw new DocumentError(ErrorType.Invalid, term);
            }

            scalar = values.get(0);
        }

        if (ValueObject.isValueObject(scalar)) {
            return new LdScalar(term, scalar.asJsonObject(), required);
        }

        if (JsonUtils.isNull(scalar)) {
            if (required) {
                throw new DocumentError(ErrorType.Missing, term);
            }
            return new LdScalar(term, null, false);
        }

        throw new DocumentError(ErrorType.Invalid, term);
    }
}
