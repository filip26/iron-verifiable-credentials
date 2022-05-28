package com.apicatalog.lds.proof;

import java.time.Instant;


public interface ProofOptions {
    
    VerificationMethod getVerificationMethod();
    
    Instant getCreated();
    
    String getdomain();
    
    String getType();    
}
