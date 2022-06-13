package com.apicatalog.did.key;

import java.net.URI;

import com.apicatalog.did.Did;
import com.apicatalog.did.DidUrl;
import com.apicatalog.ld.signature.key.VerificationKey;

import jakarta.json.JsonObject;

public class DidVerificationKey implements VerificationKey {
    
    protected DidUrl id;
    
    protected String type;
    
    protected Did controller;
    
    protected byte[] publicKey;
    
    protected String publicKeyMultibase;
    
    protected DidVerificationKey() {
        
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
    
    public String getPublicKeyMultibase() {
        return publicKeyMultibase;
    }

}
