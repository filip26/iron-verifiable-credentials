package com.apicatalog.multikey;

import java.net.URI;
import java.time.Instant;

import com.apicatalog.ld.signature.key.KeyPair;

public class MultiKey implements KeyPair {

    protected static final URI TYPE = URI.create("https://w3id.org/security#Multikey");

//    protected static final URI CONTEXT = URI.create("https://w3id.org/security/multikey/v1");

//    protected static final String MULTIKEY_VOCAB = ""; // FIXME

    protected URI id;
    protected URI controller;
    
    protected String algorithm;
    
    protected byte[] publicKey;
    protected byte[] privateKey;
    
    protected Instant revoked;
    protected Instant expiration;

    @Override
    public byte[] publicKey() {
        return publicKey;
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
    
    @Override
    public String algorithm() {
        return algorithm;
    }
    
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
    
    public void setRevoked(Instant revoked) {
        this.revoked = revoked;
    }
    
    public Instant getRevoked() {
        return revoked;
    }
    
    public Instant getExpiration() {
        return expiration;
    }
    
    public void setExpiration(Instant expiration) {
        this.expiration = expiration;
    }
    
}
