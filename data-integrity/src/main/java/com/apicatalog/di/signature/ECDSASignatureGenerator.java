package com.apicatalog.di.signature;

import java.security.MessageDigest;
import java.security.SignatureException;

import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.trust.data.Data;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.signature.Signature;

public final class ECDSASignatureGenerator {

    public static Signature generateSignature(
            AsymmetricSigner signer,
            String algorithm,
            MessageDigest messageDigest,
            Proof proof,
            Data data) throws SignatureException {

//        return ProofValue.generateSignature(signer, algorithm, messageDigest, proof, data);
        return null;
    }
}
