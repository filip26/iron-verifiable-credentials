package com.apicatalog.ld.signature.algorithm;

import com.apicatalog.ld.signature.LinkedDataSuiteError;

public interface DigestAlgorithm {

    byte[] digest(byte[] data) throws LinkedDataSuiteError;

}
