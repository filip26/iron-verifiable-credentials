package com.apicatalog.ld.node;

import java.net.URI;

import com.apicatalog.jsonld.StringUtils;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.Term;
import com.apicatalog.ld.node.adapter.LdAdapter;
import com.apicatalog.oxygen.ld.LinkedNode;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public interface LdNode {

    static LdNode of(LinkedNode object) {
        return LdNodeImpl.of(object);
    }
    
    /**
     * Checks if the given {@link JsonValue} is {@link JsonObject} has the given
     * type listed as one of its <code>@type</code> declarations.
     *
     * @param type
     * @param value
     * @return <code>true</code> if the given value is {@link JsonObject} and has
     *         property @type including the given type
     */
    public static boolean isTypeOf(final String type, final JsonValue value) {

        if (StringUtils.isBlank(type)) {
            throw new IllegalArgumentException("The 'type' parameter must not be null nor blank.");
        }

        if (value == null) {
            throw new IllegalArgumentException("The 'object' parameter must not be null.");
        }

        return JsonUtils.isObject(value) && value.asJsonObject().containsKey(Keywords.TYPE)
                && JsonUtils.toStream(value.asJsonObject().get(Keywords.TYPE))
                        .filter(JsonUtils::isString).map(JsonString.class::cast)
                        .map(JsonString::getString).filter(StringUtils::isNotBlank)
                        .anyMatch(type::equals);
    }

    public static boolean hasType(final JsonValue expanded) {

        if (expanded == null) {
            throw new IllegalArgumentException("The 'expanded' parameter must not be null.");
        }

        return JsonUtils.isObject(expanded) 
                && expanded.asJsonObject().containsKey(Keywords.TYPE)
                ;
    }


    URI id() throws DocumentError;

    LdType type();

    LdScalar scalar(Term term) throws DocumentError;

    LdNode node(Term term) throws DocumentError;
    
    default boolean exists() {
        return false;
    }

    <T> T map(LdAdapter<T> adapter) throws DocumentError;
    
    static LdNode NULL = new LdNode() {
        
        @Override
        public LdType type() {
            return null;
        }
        
        @Override
        public LdScalar scalar(Term term) throws DocumentError {
            return LdScalar.NULL;
        }
        
        @Override
        public LdNode node(Term term) throws DocumentError {
            return LdNode.NULL;
        }
        
        @Override
        public <T> T map(LdAdapter<T> adapter) throws DocumentError {
            return null;
        }
        
        @Override
        public URI id() throws DocumentError {
            return null;
        }
    };
}
