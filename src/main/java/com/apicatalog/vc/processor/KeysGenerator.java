package com.apicatalog.vc.processor;

import java.net.URI;

import com.apicatalog.ld.signature.KeyGenError;
import com.apicatalog.ld.signature.LinkedDataSignature;
import com.apicatalog.ld.signature.key.KeyPair;

public final class KeysGenerator extends Processor<KeysGenerator> {

    private final LinkedDataSignature lds;

    public KeysGenerator(final LinkedDataSignature lds) {
        this.lds = lds;
    }

    /**
     * Get generated keys
     * @param id
     * @param keyLength
     *
     * @return
     * @throws KeyGenError
     */
    public KeyPair get(URI id, int keyLength) throws KeyGenError {
      return lds.keygen(id, keyLength);
    }
}
