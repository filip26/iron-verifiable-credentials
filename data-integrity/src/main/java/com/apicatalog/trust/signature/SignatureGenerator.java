package com.apicatalog.trust.signature;

import java.security.MessageDigest;
import java.security.SignatureException;
import java.util.function.Function;

import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.trust.data.Data;
import com.apicatalog.trust.proof.Proof;

@FunctionalInterface
public interface SignatureGenerator<T extends Proof> {

    Signature generate(
            String algorithm,
            AsymmetricSigner signer,
            Function<String, MessageDigest> digestFactory,
            T proof,
            Data data) throws SignatureException;
}
