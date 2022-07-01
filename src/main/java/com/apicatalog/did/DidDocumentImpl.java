package com.apicatalog.did;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import com.apicatalog.ld.signature.proof.VerificationMethod;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

class DidDocumentImpl implements DidDocument {

    protected Did id;

    protected Set<URI> alsoKnownAs;

    protected Set<Did> controller = new HashSet<>();

    protected Set<VerificationMethod> verificationMethod = new HashSet<>();

    protected Set<DidUrl> assertionMethod;
    protected Set<DidUrl> authentication;
    protected Set<DidUrl> capabilityInvocation;
    protected Set<DidUrl> capabilityDelegation;
    protected Set<DidUrl> keyAgreement;

    @Override
    public JsonObject toJson() {
        return toJson(Json.createObjectBuilder()).build();
    }

    protected JsonObjectBuilder toJson(final JsonObjectBuilder builder) {
	throw new UnsupportedOperationException();
    }

    @Override
    public Did getId() {
        return id;
    }

    @Override
    public Set<Did> getController() {
        return controller;
    }

    @Override
    public Set<VerificationMethod> getVerificationMethod() {
        return verificationMethod;
    }
}
