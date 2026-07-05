package com.apicatalog.trust.proof;

import com.apicatalog.trust.data.Data;

public interface ProofCursor {

    boolean isUnknown();

    boolean next();

    Data data();
    
    Proof proof();
}
