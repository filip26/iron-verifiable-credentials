package com.apicatalog.vc.processor;

import com.apicatalog.ld.DocumentError;

import jakarta.json.JsonValue;

/**
 * Allows to implement a custom verifiable credential status verifier
 *
 */
public interface StatusVerifier {

    /**
     * Verify the given credential status in an expanded JSON-LD form
     * 
     * @param status in an expanded JSON-LD form 
     * @throws DocumentError
     * @throws VerifyError
     */
    void verify(JsonValue status) throws DocumentError, VerifyError;

}