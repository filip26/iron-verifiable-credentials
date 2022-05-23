package com.apicatalog.vc;

import jakarta.json.JsonObject;

public interface StructuredData {

    default boolean isVerifiable() {
        return false;
    }

    default boolean isCredentials() {
        return false;
    }

    default boolean isPresentation() {
        return false;
    }

    default Verifiable asVerifiable() {
        return isVerifiable() ? (Verifiable)this : null;
    }

    default Credentials asCredentials() {
        return isCredentials() ? (Credentials)this : null;
    }

    default Presentation asPresentation() {
        return isPresentation() ? (Presentation)this : null;
    }

    static StructuredData from(JsonObject json) {

        return null;
    }
}
