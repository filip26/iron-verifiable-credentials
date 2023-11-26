package com.apicatalog.vc;

import java.net.URI;

import com.apicatalog.ld.signature.key.KeyPair;

public class TestKeyPair implements KeyPair {

    URI id;
    URI type;
    URI controller;

    byte[] publicKey;
    byte[] privateKey;

    public TestKeyPair(byte[] publicKey, byte[] privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    @Override
    public byte[] publicKey() {
        return publicKey;
    }

    @Override
    public URI id() {
        return id;
    }

    @Override
    public URI type() {
        return type;
    }

    @Override
    public URI controller() {
        return controller;
    }

    @Override
    public byte[] privateKey() {
        return privateKey;
    }

    @Override
    public String keyType() {
        return "ED25519";
    }
}
