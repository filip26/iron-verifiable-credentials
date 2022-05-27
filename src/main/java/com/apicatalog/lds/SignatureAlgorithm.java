package com.apicatalog.lds;

public interface SignatureAlgorithm {

    boolean verify(byte[] publicKey, byte[] signature, byte[] data);    
}
