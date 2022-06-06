package com.apicatalog.vc;

public interface Verifiable {

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
