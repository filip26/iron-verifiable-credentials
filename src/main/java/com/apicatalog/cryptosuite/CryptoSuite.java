package com.apicatalog.cryptosuite;

import com.apicatalog.controller.key.KeyPair;
import com.apicatalog.cryptosuite.algorithm.Canonicalizer;
import com.apicatalog.cryptosuite.algorithm.Digester;
import com.apicatalog.cryptosuite.algorithm.Signer;
import com.apicatalog.vc.model.VerifiableMaterial;

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
        if (signer == null) {
            throw new UnsupportedOperationException();
        }
        signer.verify(publicKey, signature, data);
    }

    @Override
    public byte[] sign(byte[] privateKey, byte[] data) throws SigningError {
        if (signer == null) {
            throw new UnsupportedOperationException();
        }
        return signer.sign(privateKey, data);
    }

    @Override
    public byte[] digest(byte[] data) throws CryptoSuiteError {
        if (digester == null) {
            throw new UnsupportedOperationException();
        }
        return digester.digest(data);
    }

    @Override
    public byte[] canonicalize(VerifiableMaterial document) throws CryptoSuiteError {
        if (canonicalizer != null) {
            throw new UnsupportedOperationException();
        }
        return canonicalizer.canonicalize(document);
    }

    @Override
    public KeyPair keygen() throws KeyGenError {
        if (signer == null) {
            throw new UnsupportedOperationException();
        }
        return signer.keygen();
    }

    public String id() {
        return id;
    }

    public boolean isUnknown() {
        return signer == null && canonicalizer == null && digester == null;
    }
}
