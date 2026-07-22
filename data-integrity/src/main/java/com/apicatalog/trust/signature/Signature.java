package com.apicatalog.trust.signature;

import java.security.InvalidKeyException;
import java.security.SignatureException;

import com.apicatalog.security.AsymmetricVerifier;
import com.apicatalog.security.Digestor;
import com.apicatalog.trust.payload.CanonicalPayload;
import com.apicatalog.trust.proof.Proof;

/**
 * Represents a cryptographic signature.
 * 
 * This interface provides methods to access the signature's metadata, the
 * payload it secures, the associated proof, and to verify its validity against
 * a given public key.
 */
public interface Signature {

    /**
     * Verifies the signature against the provided verifier and public key.
     *
     * @param verifier      the cryptographic verifier used for verification
     * @param digestFactory the factory used to instantiate the digestor for hashing
     *                      the payload
     * @param publicKey     the public key bytes used to verify the signature
     * @return {@code true} if the signature is valid, {@code false} otherwise
     * @throws InvalidKeyException if the provided public key is invalid
     * @throws SignatureException  if the signature verification process fails
     */
    boolean verify(
            AsymmetricVerifier verifier,
            Digestor.Factory digestFactory,
            byte[] publicKey)
            throws InvalidKeyException, SignatureException;

    /**
     * Retrieves the identifier of the cryptographic algorithm used to create this
     * signature.
     *
     * @return a string representing the signature algorithm
     */
    String algorithm();

    /**
     * Retrieves the canonical payload that this signature secures.
     *
     * @return the {@link CanonicalPayload} object
     */
    CanonicalPayload payload();

    /**
     * Retrieves the proof associated with this signature.
     *
     * @return the {@link Proof} object
     */
    Proof proof();

    /**
     * Returns the raw byte representation of the signature value.
     *
     * @return a byte array containing the raw signature bytes
     */
    byte[] toByteArray(); // TODO ?! this could be encoders task
}
