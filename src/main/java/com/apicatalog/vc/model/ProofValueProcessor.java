package com.apicatalog.vc.model;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.processor.Issuer;
import com.apicatalog.vc.processor.Verifier;

import jakarta.json.JsonObject;

/**
 * Allows to manage a proof value. Is used by {@link Verifier} and
 * {@link Issuer}.
 * 
 * @since 0.9.0
 */
public interface ProofValueProcessor {

    /**
     * Removes a proof value from the given expanded JSON-LD object and returns a
     * new object without a proof value. i.e. an unsigned proof.
     * 
     * @param expanded a proof in an expanded JSON-LD form
     * @return an unsigned proof
     */
    JsonObject removeProofValue(JsonObject expanded);

    /**
     * Sets a proof value to the given expanded JSON-LD object and returns a new
     * object, i.e. a signed proof. Overrides an existing value.
     * 
     * @param expanded
     * @param proofValue to set
     * 
     * @return a signed proof
     * @throws DocumentError
     */
    JsonObject setProofValue(JsonObject expanded, byte[] proofValue) throws DocumentError;
}
