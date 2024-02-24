package com.apicatalog.vc.keygen;

import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.KeyGenError;
import com.apicatalog.ld.signature.key.KeyPair;

public final class KeysGenerator {

    private final CryptoSuite suite;

    public KeysGenerator(final CryptoSuite suite) {
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
}
