package com.apicatalog.vc.solid;

import java.util.Objects;

import com.apicatalog.controller.key.VerificationKey;
import com.apicatalog.cryptosuite.CryptoSuite;
import com.apicatalog.cryptosuite.Signature;
import com.apicatalog.cryptosuite.VerificationError;
import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.proof.ProofValue;

/**
 * Represent a proof value used together with full disclosure suites. i.e.
 * suites do not allowing a selective disclosure.
 */
public record SolidProofValue(
        Signature signature,
        VerifiableMaterial data,
        VerifiableMaterial unsignedProof,
        Proof proof
        ) implements ProofValue {

    public static SolidProofValue of(CryptoSuite cryptoSuite, VerifiableMaterial data, VerifiableMaterial unsignedProof, byte[] signature, Proof proof) {
        return new SolidProofValue(new Signature(cryptoSuite, signature), data, unsignedProof, proof);
    }

    @Override
    public void verify(VerificationKey key) throws VerificationError {
        Objects.requireNonNull(key);

        // TODO check key type

        // verify signature
        signature.verify(
                data,
                unsignedProof,
                key.publicKey().rawBytes()
                );
    }
}
