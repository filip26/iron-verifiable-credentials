package com.apicatalog.oxygen.ld;

public interface LinkedData {

    default boolean isObject() {
        return false;
    }

    default boolean isLiteral() {
        return false;
    }
    
    default LinkedNode asObject() {
        throw new ClassCastException();
    }
    
    default LinkedLiteral asLiteral() {
        throw new ClassCastException();
    }
}
