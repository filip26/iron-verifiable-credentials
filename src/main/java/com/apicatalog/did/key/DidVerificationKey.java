package com.apicatalog.did.key;

import java.net.URI;

import com.apicatalog.did.Did;
import com.apicatalog.did.DidUrl;
import com.apicatalog.ld.signature.key.VerificationKey;

import jakarta.json.JsonObject;

public class DidVerificationKey implements VerificationKey {

    protected final DidUrl id;

    protected final String type;

    protected final Did controller;

    protected final byte[] publicKey;

    protected DidVerificationKey(DidUrl id, String type, Did controller, byte[] publicKey) {
        this.id = id;
        this.type = type;
        this.controller = controller;
        this.publicKey = publicKey;
    }

    @Override
    public URI getId() {
        return id.toUri();
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public URI getController() {
        return controller.toUri();
    }

    @Override
    public JsonObject toJson() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] getPublicKey() {
        return publicKey;
    }
}
