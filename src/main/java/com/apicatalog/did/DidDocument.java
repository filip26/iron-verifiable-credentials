package com.apicatalog.did;

import java.net.URI;
import java.util.Set;

import com.apicatalog.did.key.DidKey;
import com.apicatalog.did.key.DidVerificationKey;
import com.apicatalog.ld.signature.proof.VerificationMethod;

public class DidDocument {

    /**
     * see {@link https://www.w3.org/TR/did-core/#did-document-properties}
     */
    private URI id;
    
    
    private Set<URI> alsoKnownAs;
    
    private Set<Did> controller;

    //FIXME should be a set of methods
    private VerificationMethod signatureMethod;
    private VerificationMethod encryptiongMethod;
    
    //FIXME sets
    private URI assertionMethod;
    private URI authentication;
    private URI capabilityInvocation;
    private URI capabilityDelegation;
    private URI keyAgreement;
    
    //TODO keyAgreement, service

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
        document.signatureMethod = DidVerificationKey.createSignatureMethod(didKey);
        
        // 5.
        document.encryptiongMethod = DidVerificationKey.createEncryptionMethod(didKey); 
        
        // 6.
        document.id = didKey.toUri();
        
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
