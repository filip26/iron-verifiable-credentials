package com.apicatalog.vc.suite;

import java.net.URI;

import com.apicatalog.jsonld.schema.LdTerm;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.method.VerificationMethod;
import com.apicatalog.vc.model.Proof;

import jakarta.json.JsonObject;

/**
 * A specified set of cryptographic primitives consisting of a canonicalization
 * algorithm, a message digest algorithm, and a signature algorithm.
 */
public interface SignatureSuite {

    // proof type id
    LdTerm id();

    // JSON-LD context defining the type
    URI context();

    /**
     * Deserializes the given expanded JSON-LD object into a {@link Proof}.
     *  
     * @param expanded
     * @return {@link Proof} instance
     * @throws DocumentError
     */
    Proof readProof(JsonObject expanded) throws DocumentError;
    
    VerificationMethod readMethod(JsonObject expanded) throws DocumentError;
}
