package com.apicatalog.did;

import java.net.URI;

import com.apicatalog.ld.signature.proof.VerificationMethod;

public class DidDocumentBuilder {

    private final DidDocumentImpl document;
    
    protected DidDocumentBuilder() {
        this.document = new DidDocumentImpl();
    }
    
    public static DidDocumentBuilder create() {
        return new DidDocumentBuilder();
    }
    
    public DidDocumentBuilder id(Did did) {
        this.document.id = did;
        return this;
    }
    
    public DidDocumentBuilder add(VerificationMethod verificationMethod) {
        //TODO        
        return this;
    }

    public DidDocumentBuilder addAuthentication(DidUrl didUrl) {
        //TODO        
        return this;
    }

    public DidDocument build() {
        return document;
    }

    public DidDocumentBuilder addAssertionMethod(URI id) {
        // TODO Auto-generated method stub
        return this;
    }

    public DidDocumentBuilder addCapabilityInvocation(URI id) {
        // TODO Auto-generated method stub
        return this;
    }

    public DidDocumentBuilder addCapabilityDelegation(URI id) {
        // TODO Auto-generated method stub
        return this;        
    }
    
}
