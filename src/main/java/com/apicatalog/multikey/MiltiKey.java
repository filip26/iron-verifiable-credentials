package com.apicatalog.multikey;

import java.net.URI;

import com.apicatalog.ld.signature.key.KeyPair;

public class MiltiKey implements KeyPair {

    protected static final URI TYPE = URI.create("https://w3id.org/security#Multikey");
    
    protected URI id;
    protected URI controller;
    protected byte[] publicKey;
    protected byte[] privateKey;
    
    @Override
    public byte[] publicKey() {
        return publicKey();
    }
    
    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }
    
    @Override
    public URI id() {
        return id;
    }
    
    public void setId(URI id) {
        this.id = id;
    }

    @Override
    public URI type() {
        return TYPE;
    }

    @Override
    public URI controller() {
        return controller;
    }
    
    public void setController(URI controller) {
        this.controller = controller;
    }

    @Override
    public byte[] privateKey() {
        return privateKey;
    }
    
    public void setPrivateKey(byte[] privateKey) {
        this.privateKey = privateKey;
    }
    
    //TODO revoked
}
