package com.apicatalog.vc.di;

import com.apicatalog.controller.key.VerificationKey;
import com.apicatalog.cryptosuite.CryptoSuite;

@FunctionalInterface
public interface CryptoSuiteProvider {

    CryptoSuite getCryptoSuite(VerificationKey key);
    
}
