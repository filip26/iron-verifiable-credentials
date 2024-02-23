package com.apicatalog.vc.model;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.issuer.StandardIssuer;
import com.apicatalog.vc.processor.Verifier;

import jakarta.json.JsonObject;

/**
 * Allows to manage a proof value. Is used by {@link Verifier} and
 * {@link StandardIssuer}.
 * 
 * @since 0.9.0
 */
public interface ProofValueProcessor {

    /**
     * Removes a proof value from the given expanded JSON-LD object and returns a
     * new object without a proof value. i.e. an unsigned proof.
     * 
     * @param proof a proof in an expanded JSON-LD form
     * @return an unsigned proof
     */
    JsonObject removeProofValue(JsonObject proof);

    /**
     * Encodes and sets a proof value to the given expanded JSON-LD object and returns a new
     * object, i.e. a signed proof. Overrides an existing value.
     * 
     * @param proof a proof in an expanded form
     * @param proofValue to set
     * 
     * @return a signed proof
     * @throws DocumentError
     */
    @Deprecated
    JsonObject setProofValue(JsonObject proof, byte[] proofValue) throws DocumentError;
}
