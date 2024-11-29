package com.apicatalog.cryptosuite.algorithm;

import com.apicatalog.controller.key.KeyPair;
import com.apicatalog.cryptosuite.CryptoSuiteError;
import com.apicatalog.cryptosuite.KeyGenError;
import com.apicatalog.cryptosuite.VerificationError;

/**
 * An algorithm that takes an input message and produces an output value where
 * the receiver of the message can mathematically verify that the message has
 * not been modified in transit and came from someone possessing a particular
 * secret.
 */
public interface SignatureAlgorithm {

    void verify(byte[] publicKey, byte[] signature, byte[] data) throws VerificationError;

    byte[] sign(byte[] privateKey, byte[] data) throws CryptoSuiteError;

    KeyPair keygen() throws KeyGenError;
}