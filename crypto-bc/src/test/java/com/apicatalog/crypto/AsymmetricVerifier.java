package com.apicatalog.crypto;

import java.security.InvalidKeyException;
import java.security.SignatureException;

/**
 * Verifies data signatures compatible with W3C data integrity.
 */
@FunctionalInterface
public interface AsymmetricVerifier {

    /**
     * Verifies the signature of the provided data.
     *
     * @param publicKey the raw public key
     * @param data      the data to verify
     * @param signature the raw signature to verify
     * @return {@code true} if the signature is valid, {@code false} otherwise
     * @throws InvalidKeyException if the public key is invalid
     * @throws SignatureException  if an error occurs during the verification
     *                             operation
     */
    boolean verify(byte[] publicKey, byte[] data, byte[] signature) throws InvalidKeyException, SignatureException;

}
