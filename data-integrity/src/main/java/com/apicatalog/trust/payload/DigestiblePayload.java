package com.apicatalog.trust.payload;

import java.util.Collection;

/**
 * Represents a payload that has been transformed into a canonical byte
 * representation and is capable of storing cryptographic digests.
 *
 * This interface is utilized within a cryptographic pipeline to hold the
 * deterministic bytes required for hashing, while acting as a stateful registry
 * for the resulting digest values.
 */
public interface DigestiblePayload extends CanonicalPayload {

    /**
     * Adds, or replaces an existing, digest value for the canonical payload.
     * 
     * @param algorithm the cryptographic hash algorithm used to generate the digest
     * @param value     the computed digest value
     */
    void digest(String algorithm, byte[] value);

    /**
     * Retrieves the digest value associated with the specified algorithm.
     * 
     * @param algorithm the cryptographic hash algorithm
     * @return the digest value, or null if caching is ignored or no digest exists
     */
    @Deprecated
    byte[] digest(String algorithm);

    //TODO just return immutable map<String, byte[]> and use IDENTITY for c14n
    
    /**
     * Returns the collection of all algorithms currently associated with cached
     * digests in this payload.
     * 
     * @return a collection of algorithm identifiers
     */
    @Deprecated
    Collection<String> digestAlgorithms();
}
