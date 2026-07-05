package com.apicatalog.trust.data;

/**
 * Represents a payload that has been transformed into a canonical byte
 * representation.
 *
 * This interface is utilized within a cryptographic pipeline to hold the
 * deterministic bytes required for hashing.
 */
public interface CanonicalPayload {

    /**
     * Returns the canonical, normalized byte representation. This payload serves as
     * the deterministic input for cryptographic operations.
     *
     * @return the byte array representing the canonical document
     */

    byte[] canonicalPayload();

    /**
     * Returns the algorithm used to canonicalize the payload.
     * 
     * @return the canonicalization (c14n) algorithm identifier or name
     */
    String c14n();
}
