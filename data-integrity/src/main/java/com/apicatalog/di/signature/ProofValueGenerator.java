package com.apicatalog.di.signature;

import java.security.SignatureException;

import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.security.Digestor;
import com.apicatalog.trust.payload.DigestiblePayload;
import com.apicatalog.trust.signature.Signature;

public class ProofValueGenerator {

    public static Signature generateWithSHA256(
            String algorithm,
            AsymmetricSigner signer,
            Digestor.Factory digestFactory,
            DataIntegrityProof proof,
            DigestiblePayload data)
            throws SignatureException {
        return generate(algorithm, signer, Digestor.SHA_256, digestFactory, proof, data);
    }

    public static Signature generateWithSHA384(
            String algorithm,
            AsymmetricSigner signer,
            Digestor.Factory digestFactory,
            DataIntegrityProof proof,
            DigestiblePayload data)
            throws SignatureException {
        return generate(algorithm, signer, Digestor.SHA_384, digestFactory, proof, data);
    }

    public static Signature generate(
            String algorithm,
            AsymmetricSigner signer,
            String digestAlgorithm,
            Digestor.Factory digestFactory,
            DataIntegrityProof proof,
            DigestiblePayload payload)
            throws SignatureException {

        var digestor = digestFactory.newDigestor(digestAlgorithm);

        return ProofValue.generateSignature(
                algorithm,
                digestAlgorithm,
                signer,
                digestor,
                proof,
                payload);
    }
}
