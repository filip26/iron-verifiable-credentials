package com.apicatalog.controller.method;

public interface VerificationKey extends VerificationMethod {

    String algorithm();
    
    byte[] publicKey();

}
