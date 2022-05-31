package com.apicatalog.lds.algorithm;

/**
 * An algorithm that takes an input message and produces an output value
 * where the receiver of the message can mathematically verify that the message
 * has not been modified in transit and came from someone possessing a particular secret.
 */
public interface SignatureAlgorithm {

    boolean verify(byte[] publicKey, byte[] signature, byte[] data);

    byte[] sign(byte[] privateKey, byte[] data);    
}
