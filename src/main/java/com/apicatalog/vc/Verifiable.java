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
        
        if (presentation instanceof MutableVerifiablePresentation) {
            ((MutableVerifiablePresentation)presentation).setProof(proof);
            return ((MutableVerifiablePresentation)presentation); 
        }
        
        return new MutableVerifiablePresentation(presentation, proof);
    }
}
