package com.apicatalog.cryptosuite.algorithm;

import com.apicatalog.cryptosuite.CryptoSuiteError;

public interface DigestAlgorithm {

    byte[] digest(byte[] data) throws CryptoSuiteError;

}
