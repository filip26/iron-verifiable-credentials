package com.apicatalog.vc.solid;

import java.util.Objects;

import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.Signature;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.VerificationError.VerificationErrorCode;
import com.apicatalog.linkedtree.LinkedTree;
import com.apicatalog.vc.proof.ProofValue;

/**
 * Represent a proof value used together with full disclosure suites. i.e.
 * suites do not allowing a selective disclosure.
 */
public record SolidProofValue(
        byte[] toByteArray) implements ProofValue {

    public SolidProofValue {
        Objects.requireNonNull(toByteArray);
    }

    @Override
    public void verify(
            CryptoSuite cryptoSuite,  
            LinkedTree data, 
            LinkedTree unsignedProof, 
            byte[] publicKey) throws VerificationError {

        Objects.requireNonNull(data);
        Objects.requireNonNull(publicKey);

        if (cryptoSuite == null) {
            throw new VerificationError(VerificationErrorCode.UnsupportedCryptoSuite);
        }

        final Signature signature = new Signature(cryptoSuite);

        // verify signature
        signature.verify(
                data,
                unsignedProof,
                publicKey,
                toByteArray);
    }
}
