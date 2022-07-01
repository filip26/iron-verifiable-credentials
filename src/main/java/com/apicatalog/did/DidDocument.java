package com.apicatalog.did;

import java.util.Set;

import com.apicatalog.ld.signature.proof.VerificationMethod;

import jakarta.json.JsonObject;

/**
 * DID Document
 * 
 * @see {@link <a href="https://www.w3.org/TR/did-core/#did-document-properties">DID document properties</a>}
 */

public interface DidDocument {

    Did getId();

    Set<Did> getController();

    Set<VerificationMethod> getVerificationMethod();

    JsonObject toJson();
}
