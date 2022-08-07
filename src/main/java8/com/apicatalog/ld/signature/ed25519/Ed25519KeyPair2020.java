package com.apicatalog.ld.signature.ed25519;

import java.net.URI;

import com.apicatalog.ld.signature.key.KeyPair;

public final class Ed25519KeyPair2020 extends Ed25519VerificationKey2020 implements KeyPair {

    private final byte[] privateKey;
    
    public Ed25519KeyPair2020(
                URI id,
                URI controller,
                String type,
                byte[] publicKey,
                byte[] privateKey
                ) {
        super(id, controller, type, publicKey);
        this.privateKey = privateKey;
    }

    @Override
    public byte[] privateKey() {
        return privateKey;
    }
}
