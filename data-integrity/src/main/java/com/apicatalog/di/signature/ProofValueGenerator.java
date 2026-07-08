package com.apicatalog.di.signature;

import java.security.MessageDigest;
import java.security.SignatureException;

import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.trust.data.Data;
import com.apicatalog.trust.signature.Signature;
import com.apicatalog.trust.signature.SignatureGenerator;

public class ProofValueGenerator implements SignatureGenerator<DataIntegrityProof> {

    private final MessageDigest digestor;

    public ProofValueGenerator(MessageDigest digestor) {
        this.digestor = digestor;
    }

    @Override
    public Signature generate(
            String algorithm,
            AsymmetricSigner signer,
            DataIntegrityProof proof,
            Data data)
            throws SignatureException {

        return ProofValue.generateSignature(
                algorithm,
                signer,
                digestor,
                proof,
                data);
    }
}
