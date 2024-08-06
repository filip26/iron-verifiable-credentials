package com.apicatalog.ld.node;

import java.net.URI;
import java.util.Collection;
import java.util.Objects;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.lang.ValueObject;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.Term;
import com.apicatalog.ld.node.adapter.LdAdapter;
import com.apicatalog.oxygen.ld.LinkedData;
import com.apicatalog.oxygen.ld.LinkedNode;

import jakarta.json.JsonArray;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

class LdNodeImpl implements LdNode {

    final LinkedNode object;

    protected LdNodeImpl(final LinkedNode object) {
        this.object = object;
    }

    public static LdNode of(final LinkedNode object) {
        Objects.requireNonNull(object);
        return new LdNodeImpl(object);
    }

    @Override
    public URI id() throws DocumentError {
        return object.id();
    }

    @Override
    public LdScalar scalar(Term term) throws DocumentError {

        final Collection<LinkedData> values = object.values(term.uri());

        if (values != null && !values.isEmpty()) {

            if (values.size() > 1) {
                throw new DocumentError(ErrorType.Invalid, term);
            }
            
            final LinkedData value = values.iterator().next();

            if (value == null || !value.isLiteral()) {
                throw new DocumentError(ErrorType.Invalid, term);
            }

            return LdScalarImpl.of(term, value.asLiteral());
        }

        return LdScalar.NULL;
    }

    @Override
    public LdNode node(Term term) throws DocumentError {
        final Collection<LinkedData> values = object.values(term.uri());

        if (values != null && !values.isEmpty()) {

//            final JsonArray expanded = values.asJsonArray();
//
//            if (expanded.isEmpty()) {
//                return LdNode.NULL;
//            }

            if (values.size() > 1) {
                throw new DocumentError(ErrorType.Invalid, term);
            }

            final LinkedData value = values.iterator().next();
            
            if (value == null || !value.isObject()) {
                throw new DocumentError(ErrorType.Invalid, term);
            }
            
//            final JsonValue value = expanded.iterator().next();
//
//            if (JsonUtils.isNotObject(value) || ValueObject.isValueObject(value)) {
//                throw new DocumentError(ErrorType.Invalid, term);
//            }

            return LdNodeImpl.of(value.asObject());

//        } else if (JsonUtils.isNotNull(values)) {
//            throw new DocumentError(ErrorType.Invalid, term);
        }

        return LdNode.NULL;
    }

    @Override
    public <T> T map(LdAdapter<T> adapter) throws DocumentError {
        return adapter.read(object);
    }

    @Override
    public LdType type() {
//        return new LdType(object);
        return null;
    }
    
    @Override
    public boolean exists() {
        return true;
    }
}
