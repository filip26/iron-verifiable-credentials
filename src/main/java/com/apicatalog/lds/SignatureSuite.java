package com.apicatalog.lds;

import com.apicatalog.rdf.RdfDataset;

import jakarta.json.JsonStructure;

/**
 * A specified set of cryptographic primitives consisting of a canonicalization algorithm,
 * a message digest algorithm, and a signature algorithm that are bundled together 
 * for the purposes of safety and convenience.
 */
public class SignatureSuite implements CanonicalizationAlgorithm, DigestAlgorithm, SignatureAlgorithm {

    private final String id;
    private final CanonicalizationAlgorithm canonicalization;
    private final DigestAlgorithm digester;
    private final SignatureAlgorithm signer;
    
    public SignatureSuite(
            String id,
            CanonicalizationAlgorithm canonicalization,
            DigestAlgorithm digester,
            SignatureAlgorithm signer
            ) {
        this.id = id;
        this.canonicalization = canonicalization;
        this.digester = digester;
        this.signer = signer;
    }
    
    @Override
    public boolean verify(byte[] publicKey, byte[] signature, byte[] data) {
        return signer.verify(publicKey, signature, data);
    }

    @Override
    public byte[] sign(byte[] privateKey, byte[] data) {
        return signer.sign(privateKey, data);
    }

    @Override
    public byte[] digest(byte[] data) {
        return digester.digest(data);
    }

    @Override
    public byte[] canonicalize(JsonStructure document) {
        return canonicalization.canonicalize(document);
    }

    public String getId() {
        return id;
    }

    @Override
    public byte[] canonicalize(RdfDataset dataset) {
        return canonicalization.canonicalize(dataset);
    }
}
