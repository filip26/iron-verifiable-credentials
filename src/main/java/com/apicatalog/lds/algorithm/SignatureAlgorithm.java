package com.apicatalog.lds.algorithm;

import com.apicatalog.lds.SigningError;
import com.apicatalog.lds.VerificationError;
import com.apicatalog.lds.key.KeyPair;

/**
 * An algorithm that takes an input message and produces an output value
 * where the receiver of the message can mathematically verify that the message
 * has not been modified in transit and came from someone possessing a particular secret.
 */
public interface SignatureAlgorithm {

    boolean verify(byte[] publicKey, byte[] signature, byte[] data) throws VerificationError;

    byte[] sign(byte[] privateKey, byte[] data) throws SigningError;
    
    KeyPair keygen( int length);    
}
