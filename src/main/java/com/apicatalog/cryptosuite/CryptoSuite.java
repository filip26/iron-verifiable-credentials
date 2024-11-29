package com.apicatalog.cryptosuite;

import java.util.Objects;

import com.apicatalog.controller.key.KeyPair;
import com.apicatalog.cryptosuite.algorithm.CanonicalizationMethod;
import com.apicatalog.cryptosuite.algorithm.DigestAlgorithm;
import com.apicatalog.cryptosuite.algorithm.SignatureAlgorithm;
import com.apicatalog.vc.model.VerifiableMaterial;

/**
 * A specified set of cryptographic primitives consisting of a canonicalization
 * algorithm, a message digest algorithm, and a signature algorithm.
 */
public class CryptoSuite implements CanonicalizationMethod, DigestAlgorithm, SignatureAlgorithm {

    protected final String name;
    protected final int keyLength;
    protected final CanonicalizationMethod canonicalizer;
    protected final DigestAlgorithm digester;
    protected final SignatureAlgorithm signer;

    public CryptoSuite(
            String name,
            int keyLength,
            CanonicalizationMethod canonicalizer,
            DigestAlgorithm digester,
            SignatureAlgorithm signer) {

        Objects.requireNonNull(name);

        this.name = name;
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
    public byte[] sign(byte[] privateKey, byte[] data) throws CryptoSuiteError {
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
        if (canonicalizer == null) {
            throw new UnsupportedOperationException();
        }
        return canonicalizer.canonicalize(document);
    }

    @Override
    public KeyPair keygen() throws CryptoSuiteError {
        if (signer == null) {
            throw new UnsupportedOperationException();
        }
        return signer.keygen();
    }

    /**
     * A cryptographic suite name used to recognize the suite. Please note the same
     * name could be used by multiple {@link CryptoSuite} using different key
     * length.
     * 
     * @return a cryptographic suite name
     */
    public String name() {
        return name;
    }

    /**
     * Key length in bites.
     * 
     * @return a key length in bites
     */
    public int keyLength() {
        return keyLength;
    }

    /**
     * Check if the suite is recognized, i.e. algorithms and configuration is known
     * to a processor.
     * 
     * @return <code>true</code> if the suite cannot be processed, is read only
     */
    public boolean isUnknown() {
        return canonicalizer == null || digester == null || signer == null;
    }
}
