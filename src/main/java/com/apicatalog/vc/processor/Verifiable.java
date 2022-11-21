package com.apicatalog.vc.processor;

import java.net.URI;

interface Verifiable {

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
