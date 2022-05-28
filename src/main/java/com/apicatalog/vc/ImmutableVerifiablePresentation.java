package com.apicatalog.vc;

import com.apicatalog.vc.proof.Proof;

import jakarta.json.JsonArray;

class ImmutableVerifiablePresentation implements VerifiablePresentation {

    final Proof proof;

    public ImmutableVerifiablePresentation(Presentation presentation, Proof proof) {
        this.proof = proof;
    }

    @Override
    public Proof getProof() {
        return proof;
    }

    @Override
    public void verify() throws VerificationError {
        // TODO Auto-generated method stub
        
    }

    public JsonArray getExpandedDocument() {
        // TODO Auto-generated method stub
        return null;
    }
    
}
