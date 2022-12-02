package com.apicatalog.vc.integrity;

import java.net.URI;

import com.apicatalog.ld.signature.key.KeyPair;

public class DataIntegrityKeyPair implements KeyPair {

    final URI id;
    final URI type;
    final URI controller;

    final byte[] publicKey;
    final byte[] privateKey;

    protected DataIntegrityKeyPair(URI id, URI type, URI controller, byte[] publicKey, byte[] privateKey) {
        this.id = id;
        this.type = type;
        this.controller = controller;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public static DataIntegrityKeyPair createVerificationKey(URI id, URI type, URI controller, byte[] publicKey) {
        return new DataIntegrityKeyPair(id, type, controller, publicKey, null);
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

}
