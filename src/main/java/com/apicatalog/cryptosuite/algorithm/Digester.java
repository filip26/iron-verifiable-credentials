package com.apicatalog.cryptosuite.algorithm;

import com.apicatalog.cryptosuite.CryptoSuiteError;

public interface Digester {

    byte[] digest(byte[] data) throws CryptoSuiteError;

}
