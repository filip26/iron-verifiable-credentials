package com.apicatalog.did;

import java.net.URI;

import com.apicatalog.ld.signature.proof.VerificationMethod;

public class DidDocument {

    private URI id;
    
    private VerificationMethod signatureMethod;
    private VerificationMethod encryptiongMethod;
    
    private URI assertionMethod;
    private URI authentication;
    private URI capabilityInvocation;
    private URI capabilityDelegation;
    private URI keyAgreement;
    
    /**
     * Creates a new DID document by expanding the DID key.
     * 
     * see {@link https://pr-preview.s3.amazonaws.com/w3c-ccg/did-method-key/pull/51.html#document-creation-algorithm}
     * 
     * @return The new DID document
     */
    public DidDocument expand(final DidKey didKey) {
        
        final DidDocument document = new DidDocument();

        // 4.
        document.signatureMethod = DidVerificationKey.expand(didKey);
        
        // 5.
        //TODO document.encryptiongMethod = 
        
        // 6.
        document.id = didKey.toURI();
        
        // 7.
        //TODO toJson();
        
        // 8.
        this.authentication = signatureMethod.getId();
        this.assertionMethod = signatureMethod.getId();
        this.capabilityInvocation = signatureMethod.getId();
        this.capabilityDelegation = signatureMethod.getId();
        
        // 9.        
        //TODO
        
        return document;   
    }
}
