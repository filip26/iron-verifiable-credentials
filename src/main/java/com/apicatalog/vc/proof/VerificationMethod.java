package com.apicatalog.vc.proof;

import java.net.URI;

import com.apicatalog.lds.KeyPair;

public interface VerificationMethod {

    URI getId();
    
    KeyPair get();
    
}
