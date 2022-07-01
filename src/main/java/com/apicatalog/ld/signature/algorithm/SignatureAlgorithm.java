package com.apicatalog.ld.signature.algorithm;

import com.apicatalog.ld.signature.KeyGenError;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.key.KeyPair;

/**
 * An algorithm that takes an input message and produces an output value
 * where the receiver of the message can mathematically verify that the message
 * has not been modified in transit and came from someone possessing a particular secret.
 */
public interface SignatureAlgorithm {

    void verify(byte[] publicKey, byte[] signature, byte[] data) throws VerificationError;

    byte[] sign(byte[] privateKey, byte[] data) throws SigningError;

    KeyPair keygen(int length) throws KeyGenError;
}
