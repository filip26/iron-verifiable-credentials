package com.apicatalog.ld.signature;

import java.net.URI;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.schema.LdTerm;
import com.apicatalog.ld.signature.method.VerificationMethod;
import com.apicatalog.ld.signature.proof.Proof;
import com.apicatalog.ld.signature.proof.ProofOptions;

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
