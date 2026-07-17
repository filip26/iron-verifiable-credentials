package com.apicatalog.trust.proof;

import com.apicatalog.trust.document.Data;

public interface ProofCursor {

    boolean next();

    Data data();

    boolean isAccepted();
    
// ???   String proofType();

    Proof proof();
}
