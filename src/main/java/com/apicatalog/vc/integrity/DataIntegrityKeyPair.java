package com.apicatalog.vc.integrity;

import java.net.URI;

import com.apicatalog.ld.signature.key.MulticodecKey;
import com.apicatalog.ld.signature.key.KeyPair;

public class DataIntegrityKeyPair implements KeyPair {

    final URI id;
    final URI type;
    final URI controller;

    final MulticodecKey publicKey;
    final MulticodecKey privateKey;

    protected DataIntegrityKeyPair(URI id, URI type, URI controller, MulticodecKey publicKey, MulticodecKey privateKey) {
        this.id = id;
        this.type = type;
        this.controller = controller;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    /*
     * Use MiltiKey
     * 
     */
    @Deprecated
    public static DataIntegrityKeyPair createVerificationKey(URI id, URI type, URI controller, MulticodecKey publicKey) {
        return new DataIntegrityKeyPair(id, type, controller, publicKey, null);
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
