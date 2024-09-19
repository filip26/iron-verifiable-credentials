package com.apicatalog.ld.signature.key;

import com.apicatalog.controller.method.VerificationMethod;

public interface VerificationKey extends VerificationMethod {

    String algorithm();
    
    byte[] publicKey();

}
