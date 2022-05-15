package com.apicatalog.vc;

class MutableVerifiablePresentation implements VerifiablePresentation {

    Proof proof;
    
    public MutableVerifiablePresentation(Presentation presentation, Proof proof) {
        this.proof = proof;
    }
    
    @Override
    public Proof getProof() {
        return proof;
    }

    public void setProof(Proof proof) {
        this.proof = proof;
    }

}
