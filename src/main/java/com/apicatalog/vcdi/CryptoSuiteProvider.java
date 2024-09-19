package com.apicatalog.vcdi;

import com.apicatalog.controller.method.VerificationKey;
import com.apicatalog.ld.signature.CryptoSuite;

@FunctionalInterface
public interface CryptoSuiteProvider {

    CryptoSuite getCryptoSuite(VerificationKey key);
    
}
