package com.apicatalog.multikey;

import java.net.URI;

import com.apicatalog.jsonld.JsonLdReader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.ld.signature.key.MulticodecKey;

import jakarta.json.JsonObject;

public class MultiKey implements KeyPair {

    protected static final URI TYPE = URI.create("https://w3id.org/security#Multikey");
    
    protected static final URI CONTEXT = URI.create("https://w3id.org/security/multikey/v1");
    
    protected URI id;
    protected URI controller;
    protected MulticodecKey publicKey;
    protected MulticodecKey privateKey;
    
    @Override
    public MulticodecKey publicKey() {
        return publicKey();
    }
    
    public void setPublicKey(MulticodecKey publicKey) {
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
    public MulticodecKey privateKey() {
        return privateKey;
    }
    
    public void setPrivateKey(MulticodecKey privateKey) {
        this.privateKey = privateKey;
    }
    
    public static VerificationMethod readMethod(JsonObject expanded) throws DocumentError {
        
        if (expanded == null) {
            throw new IllegalArgumentException("Verification method cannot be null.");
        }
        
        if (!JsonLdReader.isTypeOf(TYPE.toString(), expanded)) {
//            throw new DocumentError(ErrorType.Invalid, ");
        }
        //TODO
        return null;
    }
    
    //TODO revoked
}
