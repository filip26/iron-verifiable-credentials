package com.apicatalog.did;

import java.util.Set;

import com.apicatalog.ld.signature.proof.VerificationMethod;

import jakarta.json.JsonObject;

/**
 * @see {@link https://www.w3.org/TR/did-core/#did-document-properties}
 */

public interface DidDocument {

    
    Did getId();
    
    Set<Did> getController();
    
    Set<VerificationMethod> getVerificationMethod();
    
    //TODO getters
    
    JsonObject toJson();

}
