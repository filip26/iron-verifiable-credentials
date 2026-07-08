package com.apicatalog.trust.signature;

import java.security.SignatureException;

import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.trust.data.Data;
import com.apicatalog.trust.proof.Proof;

@FunctionalInterface
public interface SignatureGenerator<T extends Proof> {

    Signature generate(
            String algorithm,
            AsymmetricSigner signer,
            T proof,
            Data data) throws SignatureException;
}
