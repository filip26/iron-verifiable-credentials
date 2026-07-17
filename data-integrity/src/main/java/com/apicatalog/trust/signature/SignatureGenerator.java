package com.apicatalog.trust.signature;

import java.security.SignatureException;

import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.security.Digestor;
import com.apicatalog.trust.payload.DigestiblePayload;
import com.apicatalog.trust.proof.Proof;

@FunctionalInterface
public interface SignatureGenerator<T extends Proof> {

    Signature generate(
            String algorithm,
            AsymmetricSigner signer,
            Digestor.Factory digestFactory,
            T proof,
            DigestiblePayload payload) throws SignatureException;
}
