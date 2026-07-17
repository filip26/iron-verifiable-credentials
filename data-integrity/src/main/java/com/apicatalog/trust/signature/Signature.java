package com.apicatalog.trust.signature;

import java.security.InvalidKeyException;
import java.security.SignatureException;

import com.apicatalog.security.AsymmetricVerifier;
import com.apicatalog.security.Digestor;
import com.apicatalog.trust.payload.CanonicalPayload;
import com.apicatalog.trust.proof.Proof;

public interface Signature {

    /**
     * Verifies the signature against the provided verifier and public key.
     *
     * @param verifier      the cryptographic verifier used for verification
     * @param digestFactory
     * @param publicKey     the public key bytes used to verify the signature
     * @return <code>true</code> if the signature is valid, <code>false</code>
     *         otherwise
     * @throws InvalidKeyException if the public key is invalid
     * @throws SignatureException  if the signature verification process fails
     */
    boolean verify(
            AsymmetricVerifier verifier,
            Digestor.Factory digestFactory,
            byte[] publicKey)
            throws InvalidKeyException, SignatureException;

    String algorithm();

    CanonicalPayload payload();

    Proof proof();

    byte[] toByteArray(); // TODO ?! this could be encoders task
}
