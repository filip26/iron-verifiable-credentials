package com.apicatalog.ld.signature.algorithm;

import com.apicatalog.ld.signature.DataError;

public interface DigestAlgorithm {

    byte[] digest(byte[] data) throws DataError;

}
