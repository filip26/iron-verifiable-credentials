package com.apicatalog.lds.proof;

import java.net.URI;

import com.apicatalog.lds.VerificationKey;

public interface VerificationMethod {

    URI getId();
    
    VerificationKey get();
    
}
