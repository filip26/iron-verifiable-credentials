package com.apicatalog.vc;

import com.apicatalog.lds.proof.Proof;

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
    
}
