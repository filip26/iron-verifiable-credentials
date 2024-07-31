package com.apicatalog.oxygen.ld;

public interface LinkedLiteral extends LinkedData {

    String type();

    @Override
    default boolean isLiteral() {
        return true;
    }
    
    @Override
    default LinkedLiteral asLiteral() {
        return this;
    }
}
