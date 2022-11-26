package com.apicatalog.ld.signature.proof;

import com.apicatalog.ld.signature.SignatureSuite;

public class DataIntegrityProofOptions extends DataIntegrityProof implements ProofOptions {

    protected final SignatureSuite suite;
    
    public DataIntegrityProofOptions(final SignatureSuite suite) {
        this.suite = suite;
        this.type = suite.getId();
    }

    @Override
    public SignatureSuite getSuite() {
        return suite;
    }

    @Override
    public Proof toUnsignedProof() {
        return this;
    }
}
