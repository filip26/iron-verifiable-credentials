package com.apicatalog.ld.signature.algorithm;

public interface DigestAlgorithm {

    byte[] digest(byte[] data);

}
