package com.apicatalog.trust;

import java.security.InvalidKeyException;
import java.security.SignatureException;

import com.apicatalog.security.AsymmetricVerifier;

public interface AtomicSignature extends Signature {

    /**
     * Verifies the signature against the provided verifier and public key.
     *
     * @param verifier  the cryptographic verifier used for verification
     * @param publicKey the public key bytes used to verify the signature
     * @return <code>true</code> if the signature is valid, <code>false</code>
     *         otherwise
     * @throws InvalidKeyException if the public key is invalid
     * @throws SignatureException  if the signature verification process fails
     */
    boolean verify(AsymmetricVerifier verifier, byte[] publicKey) throws InvalidKeyException, SignatureException;
}
