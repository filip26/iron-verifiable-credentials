package com.apicatalog.vc;

import java.net.URI;

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
