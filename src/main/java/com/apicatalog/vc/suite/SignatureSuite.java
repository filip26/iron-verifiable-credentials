package com.apicatalog.vc.suite;

import java.net.URI;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.vc.model.Proof;
import com.apicatalog.vc.model.ProofValueProcessor;

import jakarta.json.JsonObject;

/**
 * A specified set of cryptographic primitives consisting of a canonicalization
 * algorithm, a message digest algorithm, and a signature algorithm.
 */
public interface SignatureSuite {

    // proof type id
    String id();

    // JSON-LD context defining the type
    String context();
    
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
