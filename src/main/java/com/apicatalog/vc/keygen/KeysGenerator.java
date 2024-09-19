package com.apicatalog.vc.keygen;

import com.apicatalog.controller.method.KeyPair;
import com.apicatalog.cryptosuite.CryptoSuite;
import com.apicatalog.cryptosuite.KeyGenError;

public class KeysGenerator {

    protected final CryptoSuite suite;

    protected KeysGenerator(final CryptoSuite suite) {
        this.suite = suite;
    }

    /**
     * Get generated keys
     *
     * @return the generated key pair instance
     * @throws KeyGenError
     */
    public KeyPair get() throws KeyGenError {
        return suite.keygen();
    }
    
    /**
     * Generates public/private key pair.
     *
     * @param suite used to generate a key pair.
     *
     * @return {@link KeyGenError} allowing to set options and to generate key pair
     *
     * @throws KeyGenError
     */
    public static KeysGenerator with(final CryptoSuite suite) throws KeyGenError {
        if (suite == null) {
            throw new IllegalArgumentException("The cryptoSuite parameter must not be null.");
        }
        return new KeysGenerator(suite);
    }
}
