package com.apicatalog.oxygen.ld;

import java.util.Optional;

public interface LinkedLiteral extends LinkedData {

    @Override
    default boolean isLiteral() {
        return true;
    }
    
    @Override
    default LinkedLiteral asLiteral() {
        return this;
    }
    
    default boolean isString() {
        return false;
    }
    
    default String asString() {
        throw new ClassCastException();
    }
    
    /**
     * Get the lexical value of the literal.
     *
     * @return lexical value, never <code>null</code>
     */
//    @Override
//    String value();

    /**
     * An absolute IRI denoting the datatype IRI of the literal. If the value is
     * rdf:langString, {@link #language()} value is present.
     *
     * @return an absolute IRI, never <code>null</code>
     */
    String datatype();

    /**
     * An optional language tag. If this value is specified, {@link #datatype()} returns rdf:langString.
     *
     * @return language tag or {@link Optional#empty()} if not set
     */
//    Optional<String> language();

}
