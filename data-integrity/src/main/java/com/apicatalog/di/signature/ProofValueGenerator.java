package com.apicatalog.di.signature;

import java.security.MessageDigest;
import java.security.SignatureException;
import java.util.function.Function;

import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.trust.data.Data;
import com.apicatalog.trust.signature.Signature;
import com.apicatalog.trust.signature.SignatureGenerator;

public class ProofValueGenerator implements SignatureGenerator<DataIntegrityProof> {

    private final String digestAlgorithm;

    public ProofValueGenerator(String digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
    }

    @Override
    public Signature generate(
            String algorithm,
            AsymmetricSigner signer,
            Function<String, MessageDigest> digestFactory,
            DataIntegrityProof proof,
            Data data)
            throws SignatureException {

        var digestor = digestFactory.apply(digestAlgorithm);
        
        return ProofValue.generateSignature(
                algorithm,
                signer,
                digestor,
                proof,
                data);
    }
}
