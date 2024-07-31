package com.apicatalog.oxygen.ld;

import java.util.Collection;
import java.util.Optional;

public interface LinkedData {

    Collection<String> terms();

    Optional<Collection<LinkedData>> term(String name);

    default boolean isObject() {
        return false;
    }

    default boolean isLiteral() {
        return false;
    }
    
    default LinkedObject asObject() {
        throw new ClassCastException();
    }
    
    default LinkedLiteral asLiteral() {
        throw new ClassCastException();
    }
}
