package com.apicatalog.ld.signature.proof;

import com.apicatalog.ld.signature.SignatureSuite;

import jakarta.json.JsonObject;

public interface ProofOptions {

    SignatureSuite getSuite();

    JsonObject toUnsignedProof();
    
//    ProofOptions set(Property property, String value);
    // .set(prop, 
}
