package com.apicatalog.ld.node;

import java.net.URI;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.Term;
import com.apicatalog.ld.adapter.LdAdapter;

import jakarta.json.JsonObject;

public interface LdNode {

    static LdNode of(JsonObject object) {
        return LdNodeImpl.of(object);
    }

    URI id() throws DocumentError;

    LdTypeGetter type();

    LdScalar scalar(Term term) throws DocumentError;

    LdNode node(Term term) throws DocumentError;

    <T> T map(LdAdapter<T> adapter) throws DocumentError;
    
    static LdNode NULL = new LdNode() {
        
        @Override
        public LdTypeGetter type() {
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
