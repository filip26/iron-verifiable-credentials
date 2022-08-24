package com.apicatalog.ld.signature.jws;

import com.apicatalog.ld.signature.*;
import com.apicatalog.ld.signature.algorithm.CanonicalizationAlgorithm;
import com.apicatalog.ld.signature.algorithm.DigestAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import jakarta.json.JsonStructure;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Based on {@link SignatureSuite}
 *
 * @author petr apeltauer, KAPRION Technologies GmbH
 */
public class JwsSignatureSuite implements CanonicalizationAlgorithm, DigestAlgorithm, JwsSignatureAlgorithm {

    protected final String id;

    protected final CanonicalizationAlgorithm canonicalization;
    protected final DigestAlgorithm digester;
    protected final JwsSignatureAlgorithm signer;

    protected final JwsProofJsonAdapter proofAdapter;

    final String alg;

    public JwsSignatureSuite(
            final String id,
            final CanonicalizationAlgorithm canonicalization,
            final DigestAlgorithm digester,
            final JwsSignatureAlgorithm signer,
            final JwsProofJsonAdapter proofAdapter,
            final String alg
    ) {
        this.id = id;
        this.canonicalization = canonicalization;
        this.digester = digester;
        this.signer = signer;
        this.proofAdapter = proofAdapter;
        this.alg = alg;
    }

    @Override
    public boolean verify(JWK publicKey, String jws, byte[] data) throws VerificationError {
        return signer.verify(publicKey, jws, data);
    }

    @Override
    public String sign(JWK privateKey, byte[] data) throws SigningError {
        return signer.sign(privateKey, data);
    }

    @Override
    public boolean verify(PublicKey publicKey, String keyId, String jws, byte[] data) throws VerificationError {
        return signer.verify(publicKey, keyId, jws, data);
    }

    @Override
    public String sign(PrivateKey privateKey, PublicKey publicKey, String keyId, byte[] data) throws SigningError {
        return signer.sign(privateKey, publicKey, keyId, data);
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
    public JWK keygen(int length) throws KeyGenError {
        return signer.keygen(length);
    }

    public String getId() {
        return id;
    }

    public JwsProofJsonAdapter getProofAdapter() {
        return proofAdapter;
    }


}

