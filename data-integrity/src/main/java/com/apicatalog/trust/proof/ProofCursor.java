package com.apicatalog.trust.proof;

import com.apicatalog.trust.data.Data;

public interface ProofCursor {

    boolean next();

    Data data();

    boolean isAccepted();
    
// ???   String proofType();

    Proof proof();
}
