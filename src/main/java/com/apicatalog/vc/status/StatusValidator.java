package com.apicatalog.vc.status;

import com.apicatalog.ld.DocumentError;

import jakarta.json.JsonValue;

/**
 * Allows to implement a custom verifiable credential status verifier
 *
 */
public interface StatusValidator {

    /**
     * Verify the given credential status in an expanded JSON-LD form
     * 
     * @param status in an expanded JSON-LD form
     * @throws DocumentError
     */
    void verify(JsonValue status) throws DocumentError;

}