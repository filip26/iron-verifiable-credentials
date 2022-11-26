package com.apicatalog.ld.signature.proof;

import com.apicatalog.jsonld.Property;
import com.apicatalog.ld.signature.SignatureSuite;

public interface ProofOptions {

    SignatureSuite getSuite();

    Proof toUnsignedProof();
    
//    ProofOptions set(Property property, String value);
    // .set(prop, 
}
