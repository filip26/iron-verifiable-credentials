package com.apicatalog.trust.proof;

import java.time.Instant;
import java.util.Collection;

import com.apicatalog.trust.payload.CanonicalPayload;
import com.apicatalog.trust.signature.Signature;

/**
 * Represents a generic data integrity proof.
 * 
 * This interface serves as the foundational structure for all proofs, providing
 * the essential properties required to determine the intent, verification
 * method, and cryptographic authenticity of a payload.
 */
public interface Proof extends CanonicalPayload {

    /**
     * Retrieves the specific type of the proof.
     *
     * @return a string identifying the proof type
     */
    String type();

    /**
     * Retrieves the cryptographic signature associated with this proof. If a
     * signature is present, the proof is considered signed and its authenticity can
     * be verified against the canonical representation.
     *
     * @return the {@link Signature} object, or {@code null} if the proof is
     *         unsigned
     */
    Signature signature();

    /**
     * Retrieves the identifier or URL required to independently verify the proof.
     * 
     * This typically points to the distributed identifier, cryptographic public
     * key, or verification material used to evaluate the signature. This property
     * is mandatory.
     *
     * @return a string representing the URL or identifier of the verification
     *         method
     */
    String verificationMethod();

    /**
     * Retrieves the intent behind the proof's creation.
     * 
     * This indicates the reason why an entity created the proof (e.g.,
     * assertionMethod or authentication). This property is mandatory.
     *
     * @return a URI identifying the proof purpose
     */
    String purpose();

    /**
     * Retrieves the exact date and time the proof was created.
     * 
     * This property is mandatory.
     *
     * @return an {@link Instant} representing the creation timestamp
     */
    Instant created();

//    // an ordered set of proof contexts, optional, never return null but an empty
//    // set in that case
//    Collection<String> context();
}
