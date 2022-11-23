package com.apicatalog.vc.processor;

import com.apicatalog.ld.DocumentError;

import jakarta.json.JsonValue;

/**
 * Allows to implement a custom verifiable credential subject verifier.
 * 
 * @since 0.8.1
 */
public interface SubjectVerifier {

    /**
     * Verify the given subject claims in an expanded JSON-LD
     * 
     * @param subject an expanded JSON-LD 
     * @throws DocumentError
     * @throws VerifyError
     */
    void verify(JsonValue subject) throws DocumentError, VerifyError;    
    
}
