package com.apicatalog.vc.suite;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.model.Proof;

import jakarta.json.JsonObject;

/**
 * A specified set of cryptographic primitives consisting of a canonicalization
 * algorithm, a message digest algorithm, and a signature algorithm.
 */
public interface SignatureSuite {

    /**
     * Check if the given proof can be processed by the suite.
     * 
     * @param proofType     an URI representing a proof JSON-LD type
     * @param expandedProof a proof in an expanded JSON-LD form
     * 
     * @return <code>true</code> if the proof is supported, <code>false</code>
     *         otherwise
     */
    boolean isSupported(String proofType, JsonObject expandedProof);

    /**
     * Deserialize the given expanded JSON-LD object into a {@link Proof}.
     * 
     * @param expanded JSON-LD object in an expanded form
     * 
     * @return a new {@link Proof} instance
     * @throws DocumentError if the given object cannot be deserialized
     */
    Proof readProof(JsonObject expanded) throws DocumentError;
}
