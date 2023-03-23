package com.apicatalog.vc.model;

import java.net.URI;

/**
 * Represents a common ancestor for verifiable data.
 *  
 * @since 0.9.0
 *
 */
public interface Verifiable {

    URI getId();

    default boolean isCredential() {
        return false;
    }

    default boolean isPresentation() {
        return false;
    }

    default Credential asCredential() {
        throw new ClassCastException();
    }

    default Presentation asPresentation() {
        throw new ClassCastException();
    }
}
