package com.apicatalog.vc.api;

import java.net.URI;

import com.apicatalog.ld.signature.KeyGenError;
import com.apicatalog.ld.signature.LinkedDataSignature;
import com.apicatalog.ld.signature.key.KeyPair;

public final class KeyGenApi extends CommonApi<KeyGenApi> {

    private final LinkedDataSignature lds;

    protected KeyGenApi(final LinkedDataSignature lds) {
        this.lds = lds;
    }

    /**
     * Get generated keys
     *
     * @return
     */
    public KeyPair get(URI id, int keyLength) throws KeyGenError {
      return lds.keygen(id, keyLength);
    }
}
