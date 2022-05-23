package com.apicatalog.vc;

public interface Verifiable extends StructuredData {

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
}
