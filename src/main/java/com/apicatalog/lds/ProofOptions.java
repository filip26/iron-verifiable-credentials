package com.apicatalog.lds;

import java.time.Instant;

import com.apicatalog.vc.proof.VerificationMethod;


public interface ProofOptions {
    
    VerificationMethod getVerificationMethod();
    
    Instant getCreated();
    
    String getdomain();
    
    String getType();    
}
