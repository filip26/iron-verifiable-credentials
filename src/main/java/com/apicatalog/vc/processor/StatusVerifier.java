package com.apicatalog.vc.processor;

import com.apicatalog.ld.DocumentError;

import jakarta.json.JsonValue;

/**
 * Allows to implement a custom verifiable credential status verifier
 *
 */
public interface StatusVerifier {

    void verify(JsonValue status) throws DocumentError, VerifyError;

}