package com.apicatalog.vc;

import com.apicatalog.lds.proof.Proof;

public interface Verifiable {

    Proof getProof();

    default boolean isVerifiable() {
        return true;
    }

    static VerifiableCredentials from(Credentials credentials, Proof proof) {
        return null;
    }

    static VerifiablePresentation from(Presentation presentation, Proof proof) {
        return new ImmutableVerifiablePresentation(presentation, proof);
    }
}
