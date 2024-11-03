package com.apicatalog.vcdi;

import com.apicatalog.controller.key.VerificationKey;
import com.apicatalog.cryptosuite.CryptoSuite;

@FunctionalInterface
public interface CryptoSuiteProvider {

    CryptoSuite getCryptoSuite(VerificationKey key);
    
}
