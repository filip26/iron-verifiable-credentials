package com.apicatalog.ld.signature;

import com.apicatalog.ld.signature.algorithm.CanonicalizationAlgorithm;
import com.apicatalog.ld.signature.algorithm.DigestAlgorithm;
import com.apicatalog.ld.signature.algorithm.SignatureAlgorithm;
import com.apicatalog.ld.signature.key.KeyPair;

import jakarta.json.JsonStructure;

/**
 * A specified set of cryptographic primitives consisting of a canonicalization
 * algorithm, a message digest algorithm, and a signature algorithm.
 */
public class CryptoSuite implements CanonicalizationAlgorithm, DigestAlgorithm, SignatureAlgorithm {

    protected final String id;

    protected final CanonicalizationAlgorithm canonicalization;
    protected final DigestAlgorithm digester;
    protected final SignatureAlgorithm signer;

    /*
     * https://www.w3.org/TR/vc-data-integrity/#verification-material A
     * cryptographic suite specification is responsible for specifying the
     * verification method
     */
    public CryptoSuite(
            final String id,
            final CanonicalizationAlgorithm canonicalization,
            final DigestAlgorithm digester,
            final SignatureAlgorithm signer) {
        this.id = id;
        this.canonicalization = canonicalization;
        this.digester = digester;
        this.signer = signer;
    }

    @Override
    public void verify(byte[] publicKey, byte[] signature, byte[] data) throws VerificationError {
        signer.verify(publicKey, signature, data);
    }

    @Override
    public byte[] sign(byte[] privateKey, byte[] data) throws SigningError {
        return signer.sign(privateKey, data);
    }

    @Override
    public byte[] digest(byte[] data) throws LinkedDataSuiteError {
        return digester.digest(data);
    }

    @Override
    public byte[] canonicalize(JsonStructure document) throws LinkedDataSuiteError {
        return canonicalization.canonicalize(document);
    }

    @Override
    public KeyPair keygen(int length) throws KeyGenError {
        return signer.keygen(length);
    }

    public String getId() {
        return id;
    }
}
