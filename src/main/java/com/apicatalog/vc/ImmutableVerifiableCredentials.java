package com.apicatalog.vc;

import com.apicatalog.vc.proof.Proof;

class ImmutableVerifiableCredentials implements VerifiableCredentials {

    final Proof proof;

    public ImmutableVerifiableCredentials(Credentials credentials, Proof proof) {
        this.proof = proof;
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
        proof.verify();
    }

}
