package com.apicatalog.ld.node;

import java.net.URI;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.lang.ValueObject;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.Term;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.node.adapter.LdAdapter;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

class LdNodeImpl implements LdNode {

    final JsonObject object;

    protected LdNodeImpl(final JsonObject object) {
        this.object = object;
    }

    public static LdNode of(final JsonObject object) {
        if (object == null) {
            throw new IllegalArgumentException();
        }
        return new LdNodeImpl(object);
    }

    @Override
    public URI id() throws DocumentError {

        JsonValue id = object.get(Keywords.ID);

        if (JsonUtils.isString(id)) {
            try {
                return URI.create(((JsonString) id).getString());
            } catch (IllegalArgumentException e) {
                throw new DocumentError(ErrorType.Invalid, Keywords.ID);
            }
        } else if (JsonUtils.isNotNull(id)) {
            throw new DocumentError(ErrorType.Invalid, Keywords.ID);
        }

        return null;
    }

    @Override
    public LdScalar scalar(Term term) throws DocumentError {

        JsonValue values = object.get(term.uri());

        if (JsonUtils.isArray(values)) {

            final JsonArray expanded = values.asJsonArray();

            if (expanded.isEmpty()) {
                return LdScalar.NULL;
            }

            if (expanded.size() > 1) {
                throw new DocumentError(ErrorType.Invalid, term);
            }
            
            final JsonValue value = expanded.iterator().next();

            if (JsonUtils.isNotObject(value) || !ValueObject.isValueObject(value)) {
                throw new DocumentError(ErrorType.Invalid, term);
            }

            return LdScalarImpl.of(term, value.asJsonObject());

        } else if (JsonUtils.isNotNull(values)) {
            throw new DocumentError(ErrorType.Invalid, term);
        }

        return LdScalar.NULL;
    }

    @Override
    public LdNode node(Term term) throws DocumentError {
        final JsonValue values = object.get(term.uri());

        if (JsonUtils.isArray(values)) {

            final JsonArray expanded = values.asJsonArray();

            if (expanded.isEmpty()) {
                return LdNode.NULL;
            }

            if (expanded.size() > 1) {
                throw new DocumentError(ErrorType.Invalid, term);
            }

            final JsonValue value = expanded.iterator().next();

            if (JsonUtils.isNotObject(value) || ValueObject.isValueObject(value)) {
                throw new DocumentError(ErrorType.Invalid, term);
            }

            return LdNodeImpl.of(value.asJsonObject());

        } else if (JsonUtils.isNotNull(values)) {
            throw new DocumentError(ErrorType.Invalid, term);
        }

        return LdNode.NULL;
    }

    @Override
    public <T> T map(LdAdapter<T> adapter) throws DocumentError {
        return adapter.read(object);
    }

    @Override
    public LdType type() {
        return new LdType(object);
    }
    
    @Override
    public boolean exists() {
        return true;
    }
}
