package com.apicatalog.vc;

import com.apicatalog.vc.proof.Proof;

public interface Verifiable extends VcDocument {

    Proof getProof();

    @Override
    default boolean isVerifiable() {
        return true;
    }

    static VerifiableCredentials from(Credentials credentials, Proof proof) {
        return null;
    }

    static VerifiablePresentation from(Presentation presentation, Proof proof) {
        return new ImmutableVerifiablePresentation(presentation, proof);
    }
    
    void verify() throws VerificationError;
}
