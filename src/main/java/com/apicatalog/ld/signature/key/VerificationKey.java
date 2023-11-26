package com.apicatalog.ld.signature.key;

import com.apicatalog.ld.signature.VerificationMethod;

public interface VerificationKey extends VerificationMethod {

    String keyType();
    
    byte[] publicKey();

}
