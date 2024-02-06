package com.apicatalog.ld.node;

import java.net.URI;

import com.apicatalog.jsonld.schema.LdTerm;
import com.apicatalog.ld.DocumentError;

import jakarta.json.JsonObject;

public interface LdNode {

    static LdNode of(JsonObject object) {
        return LdNodeImpl.of(object);
    }

    URI id() throws DocumentError;

    LdTypeGetter type();

    LdScalar scalar(LdTerm term) throws DocumentError;

    LdNode node(LdTerm term) throws DocumentError;

    <T> T map(LdAdapter<T> adapter) throws DocumentError;
    
    static LdNode NULL = new LdNode() {
        
        @Override
        public LdTypeGetter type() {
            return null;
        }
        
        @Override
        public LdScalar scalar(LdTerm term) throws DocumentError {
            return LdScalar.NULL;
        }
        
        @Override
        public LdNode node(LdTerm term) throws DocumentError {
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
