package com.apicatalog.ld;

public interface LdValue {

    default boolean isLiteral() {
        return false;
    }
    
    default boolean isNode() {
        return false;
    }
    
    default boolean isObject() {
        return false;
    }
}
