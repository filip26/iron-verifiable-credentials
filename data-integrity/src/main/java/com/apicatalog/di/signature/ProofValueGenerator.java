package com.apicatalog.di.signature;

import java.security.MessageDigest;
import java.security.SignatureException;
import java.util.function.Function;

import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.trust.payload.DigestiblePayload;
import com.apicatalog.trust.signature.Signature;

public class ProofValueGenerator {

    public static Signature generateWithSHA256(
            String algorithm,
            AsymmetricSigner signer,
            Function<String, MessageDigest> digestFactory,
            DataIntegrityProof proof,
            DigestiblePayload data)
            throws SignatureException {
        return generate(algorithm, signer, "SHA-256", digestFactory, proof, data);
    }
    
    public static Signature generateWithSHA384(
            String algorithm,
            AsymmetricSigner signer,
            Function<String, MessageDigest> digestFactory,
            DataIntegrityProof proof,
            DigestiblePayload data)
            throws SignatureException {
        return generate(algorithm, signer, "SHA-384", digestFactory, proof, data);
    }
    
    public static Signature generate(
            String algorithm,
            AsymmetricSigner signer,
            String digestAlgorithm,
            Function<String, MessageDigest> digestFactory,
            DataIntegrityProof proof,
            DigestiblePayload payload)
            throws SignatureException {

        var digestor = digestFactory.apply(digestAlgorithm);
        
        return ProofValue.generateSignature(
                algorithm,
                signer,
                digestor,
                proof,
                payload);
    }
}
