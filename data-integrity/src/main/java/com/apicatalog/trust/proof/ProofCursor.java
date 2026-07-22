package com.apicatalog.trust.proof;

public interface ProofCursor {

    boolean next();

    boolean isAccepted();
    
// ???   String proofType();

    Proof proof();
}
