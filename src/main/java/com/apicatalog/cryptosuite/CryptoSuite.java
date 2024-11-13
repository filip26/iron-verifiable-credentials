package com.apicatalog.cryptosuite;

import java.util.Objects;

import com.apicatalog.controller.key.KeyPair;
import com.apicatalog.cryptosuite.algorithm.Canonicalizer;
import com.apicatalog.cryptosuite.algorithm.Digester;
import com.apicatalog.cryptosuite.algorithm.Signer;
import com.apicatalog.vc.model.VerifiableMaterial;

/**
 * A specified set of cryptographic primitives consisting of a canonicalization
 * algorithm, a message digest algorithm, and a signature algorithm.
 */
public class CryptoSuite implements Canonicalizer, Digester, Signer {

    protected final String id;
    protected final int keyLength;
    protected final Canonicalizer canonicalizer;
    protected final Digester digester;
    protected final Signer signer;

    public CryptoSuite(
            String id,
            int keyLength,
            Canonicalizer canonicalizer,
            Digester digester,
            Signer signer) {
        
        Objects.requireNonNull(id);
        
        this.id = id;
        this.keyLength = keyLength;
        this.canonicalizer = canonicalizer;
        this.digester = digester;
        this.signer = signer;
    }

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

    public int keyLength() {
        return keyLength;
    }
    
    public boolean isUnknown() {
        return canonicalizer == null || digester == null || signer == null;
    }
}
