package com.apicatalog.vc;

import java.net.URI;

import com.apicatalog.ld.signature.key.KeyPair;

public class TestKeyPair implements KeyPair {

    URI id;
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
    public String type() {
        return "";
    }

    @Override
    public URI controller() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] privateKey() {
        return privateKey;
    }

}
