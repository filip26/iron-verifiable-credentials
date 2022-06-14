package com.apicatalog.did;

import java.net.URI;
import java.util.Set;

import com.apicatalog.ld.signature.proof.VerificationMethod;

import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;

class DidDocumentImpl implements DidDocument {

    /**
     * @see {@link https://www.w3.org/TR/did-core/#did-document-properties}
     */
    protected Did id;

    protected Set<URI> alsoKnownAs;
    
    protected Set<Did> controller;

    protected Set<VerificationMethod> verificationMethod;
    
    protected Set<DidUrl> assertionMethod;
    protected Set<DidUrl> authentication;
    protected Set<DidUrl> capabilityInvocation;
    protected Set<DidUrl> capabilityDelegation;
    protected Set<DidUrl> keyAgreement;

    //TODO  service
    
    public JsonValue toJson() {
        return toJson(Json.createObjectBuilder()).build();
    }
    
    protected JsonObjectBuilder toJson(final JsonObjectBuilder builder) {
        //TODO
        return builder;
    }

}
