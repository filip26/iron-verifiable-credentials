package com.apicatalog.crypto;

import java.security.SignatureException;

/**
 * Signs data and produces a raw encoded signature compatible with W3C data
 * integrity.
 */
@FunctionalInterface
public interface AsymmetricSigner {

    /**
     * Signs the provided data.
     *
     * @param data the data to be signed
     * @return the raw encoded signature bytes
     * @throws SignatureException if an error occurs during the signing operation
     */
    byte[] sign(byte[] data) throws SignatureException;

}
