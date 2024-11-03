package com.apicatalog.cryptosuite;

import com.apicatalog.controller.key.KeyPair;
import com.apicatalog.cryptosuite.algorithm.Canonicalizer;
import com.apicatalog.cryptosuite.algorithm.Digester;
import com.apicatalog.cryptosuite.algorithm.Signer;
import com.apicatalog.linkedtree.LinkedTree;

/**
 * A specified set of cryptographic primitives consisting of a canonicalization
 * algorithm, a message digest algorithm, and a signature algorithm.
 */
public record CryptoSuite(
        String id,
        int keyLength,
        Canonicalizer canonicalizer,
        Digester digester,
        Signer signer) implements Canonicalizer, Digester, Signer {
    
    @Override
    public void verify(byte[] publicKey, byte[] signature, byte[] data) throws VerificationError {
        signer.verify(publicKey, signature, data);
    }

    @Override
    public byte[] sign(byte[] privateKey, byte[] data) throws SigningError {
        return signer.sign(privateKey, data);
    }

    @Override
    public byte[] digest(byte[] data) throws CryptoSuiteError {
        return digester.digest(data);
    }

    @Override
    public byte[] canonicalize(LinkedTree document) throws CryptoSuiteError {
        return canonicalizer.canonicalize(document);
    }

    @Override
    public KeyPair keygen() throws KeyGenError {
        return signer.keygen();
    }
    
    public String id() {
        return id;
    }
}
