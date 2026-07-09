package com.apicatalog.trust.signature;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.SignatureException;
import java.util.function.Function;

import com.apicatalog.security.AsymmetricVerifier;

public interface AtomicSignature extends Signature {

    /**
     * Verifies the signature against the provided verifier and public key.
     *
     * @param verifier  the cryptographic verifier used for verification
     * @param digestFactory
     * @param publicKey the public key bytes used to verify the signature
     * @return <code>true</code> if the signature is valid, <code>false</code>
     *         otherwise
     * @throws InvalidKeyException if the public key is invalid
     * @throws SignatureException  if the signature verification process fails
     */
    boolean verify(AsymmetricVerifier verifier, Function<String, MessageDigest> digestFactory, byte[] publicKey) throws InvalidKeyException, SignatureException;
}
