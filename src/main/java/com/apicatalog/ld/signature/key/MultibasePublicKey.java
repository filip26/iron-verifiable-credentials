package com.apicatalog.ld.signature.key;

import java.net.URI;

import com.apicatalog.multicodec.Multicodec.Codec;

public class MultibasePublicKey implements VerificationKey {

    private final URI id;
    private final String type;
    private URI controller;
    private byte[] publicKey;
    
    private Codec codec;
    
    public MultibasePublicKey(URI id, String type, byte[] publicKey, Codec codec) {
        this.id = id;
        this.type = type;
        this.publicKey = publicKey;
        this.codec = codec;
    }
    
    @Override
    public URI id() {
        return id;
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public URI controller() {
        return controller;
    }

    @Override
    public byte[] publicKey() {
        return publicKey;
    }

}
