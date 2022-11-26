package com.apicatalog.ld.signature.proof;

import com.apicatalog.ld.signature.SignatureSuite;

//FIXME -> wrong inheritance
public class ProofOptions extends DataIntegrityProof {

    SignatureSuite suite;
    
    public SignatureSuite getSuite() {
        return suite;
    }
}
