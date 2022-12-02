package com.apicatalog.vc.processor;

import com.apicatalog.ld.DocumentError;

import jakarta.json.JsonValue;

/**
 * Allows to implement a custom verifiable credential subject verifier.
 * 
 * @since 0.8.1
 */
public interface SubjectValidator {

    /**
     * Verify the given subject claims in an expanded JSON-LD form
     * 
     * @param subject in an expanded JSON-LD form
     * @throws DocumentError
     * @throws VerifyError
     */
    void verify(JsonValue subject) throws DocumentError, VerifyError;

}
