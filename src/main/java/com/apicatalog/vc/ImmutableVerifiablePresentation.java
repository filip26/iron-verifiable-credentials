package com.apicatalog.vc;

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
