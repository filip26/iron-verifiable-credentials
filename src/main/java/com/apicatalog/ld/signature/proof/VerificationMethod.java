package com.apicatalog.ld.signature.proof;

import java.net.URI;

import jakarta.json.JsonObject;

/**
 *
 * see {@link https://w3c-ccg.github.io/data-integrity-spec/#verification-methods}
 *
 */
public interface VerificationMethod {

    URI getId();

    String getType();

    URI getController();

    /**
     * Serializes the verification method as {@link JsonObject}.
     * Uses {@link #getType()} to determine {@link JsonObject} .
     *
     * @return
     */
    JsonObject toJson();
}
