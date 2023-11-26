package com.apicatalog.vc.integrity;

import java.net.URI;

import com.apicatalog.ld.signature.key.KeyPair;

public class DataIntegrityKeyPair implements KeyPair {

    final URI id;
    final URI type;
    final URI controller;

    final String curve;

    final byte[] publicKey;
    final byte[] privateKey;

    protected DataIntegrityKeyPair(URI id, URI type, URI controller, String curve, byte[] publicKey, byte[] privateKey) {
        this.id = id;
        this.type = type;
        this.controller = controller;
        this.curve = curve;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    /*
     * Use MiltiKey
     * 
     */
    @Deprecated
    public static DataIntegrityKeyPair createVerificationKey(URI id, URI type, URI controller, String curve, byte[] publicKey) {
        return new DataIntegrityKeyPair(id, type, controller, curve, publicKey, null);
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
        return curve;
    }
}
