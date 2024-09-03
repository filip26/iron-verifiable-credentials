package com.apicatalog.vc.solid;

import java.util.Objects;

import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.LinkedDataSignature;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.VerificationError.Code;
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
            throw new VerificationError(Code.UnsupportedCryptoSuite);
        }

        final LinkedDataSignature signature = new LinkedDataSignature(cryptoSuite);

        // verify signature
        signature.verify(
                data,
                unsignedProof,
                publicKey,
                toByteArray);
    }
}
