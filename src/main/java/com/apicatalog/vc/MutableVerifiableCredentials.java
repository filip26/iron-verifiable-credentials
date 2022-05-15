package com.apicatalog.vc;

class MutableVerifiableCredentials implements VerifiableCredentials {

    Proof proof;
    
    public MutableVerifiableCredentials(Credentials credentials, Proof proof) {
        this.proof = proof;
    }
    
    @Override
    public Proof getProof() {
        return proof;
    }

}
