package com.apicatalog.vc;

import com.apicatalog.ed25519.Ed25519Signature2020;
import com.apicatalog.lds.LinkedDataSignature;
import com.apicatalog.vc.proof.Proof;

import jakarta.json.JsonArray;

class ImmutableVerifiableCredentials implements VerifiableCredentials {

    final Proof proof;
    final JsonArray document;

    public ImmutableVerifiableCredentials(Credentials credentials, Proof proof, JsonArray document) {
        this.proof = proof;
        this.document = document;
    }

    @Override
    public Proof getProof() {
        return proof;
    }

    @Override
    public void verify() throws VerificationError {
        if (proof == null) {
            throw new VerificationError();
        }
        proof.verify(document);

        LinkedDataSignature ldSignature = new LinkedDataSignature(new Ed25519Signature2020());  //TODO
        ldSignature.verify(this);
    }

    @Override
    public JsonArray getExpandedDocument() {
        return document;
    }

}
