package com.apicatalog.ld.signature;

import com.apicatalog.ld.signature.algorithm.CanonicalizationAlgorithm;
import com.apicatalog.ld.signature.algorithm.DigestAlgorithm;
import com.apicatalog.ld.signature.algorithm.SignatureAlgorithm;

import jakarta.json.JsonStructure;

/**
 * A specified set of cryptographic primitives consisting of a canonicalization algorithm,
 * a message digest algorithm, and a signature algorithm.
 *
 */
public class SignatureSuite implements CanonicalizationAlgorithm, DigestAlgorithm, SignatureAlgorithm {

    private final String id;
    private final CanonicalizationAlgorithm canonicalization;
    private final DigestAlgorithm digester;
    private final SignatureAlgorithm signer;
    private final SignatureAdapter adapter;

    public SignatureSuite(
            String id,
            CanonicalizationAlgorithm canonicalization,
            DigestAlgorithm digester,
            SignatureAlgorithm signer,
            SignatureAdapter adapter
            ) {
        this.id = id;
        this.canonicalization = canonicalization;
        this.digester = digester;
        this.signer = signer;
        this.adapter = adapter;
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
    public byte[] digest(byte[] data) throws DataError {
        return digester.digest(data);
    }

    @Override
    public byte[] canonicalize(JsonStructure document) throws DataError {
        return canonicalization.canonicalize(document);
    }

    @Override
    public KeyPair keygen(int length) {
        return signer.keygen(length);
    }

    public String getId() {
        return id;
    }

    public SignatureAdapter getAdapter() {
        return adapter;
    }
}
