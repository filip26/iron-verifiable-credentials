package com.apicatalog.ld.signature.algorithm;

import com.apicatalog.ld.signature.CryptoSuiteError;

public interface Digester {

    byte[] digest(byte[] data) throws CryptoSuiteError;

}
