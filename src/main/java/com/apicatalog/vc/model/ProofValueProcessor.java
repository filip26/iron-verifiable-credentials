package com.apicatalog.vc.model;

import com.apicatalog.ld.DocumentError;

import jakarta.json.JsonObject;

public interface ProofValueProcessor {

    /**
     * Removes a proof value from the given expanded JSON-LD object and
     * returns a new object without a proof value. i.e. an unsigned proof. 
     * 
     * @param expanded a proof in an expanded JSON-LD form
     * @return an unsigned proof
     */
    JsonObject removeProofValue(JsonObject expanded);
    
    /**
     * Sets a proof value to the given expanded JSON-LD object and
     * returns a new object, i.e. a signed proof. Overrides an existing value.
     *  
     * @param expanded
     * @param proofValue to set
     * 
     * @return a signed proof
     * @throws DocumentError 
     */
    JsonObject setProofValue(JsonObject expanded, byte[] proofValue) throws DocumentError;
}
