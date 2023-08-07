package com.apicatalog.vc;

import java.net.URI;

import com.apicatalog.ld.signature.key.MulticodecKey;
import com.apicatalog.ld.signature.key.KeyPair;

public class TestKeyPair implements KeyPair {

    URI id;
    URI type;
    URI controller;

    MulticodecKey publicKey;
    MulticodecKey privateKey;

    public TestKeyPair(MulticodecKey publicKey, MulticodecKey privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    @Override
    public MulticodecKey publicKey() {
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
    public MulticodecKey privateKey() {
        return privateKey;
    }

}
