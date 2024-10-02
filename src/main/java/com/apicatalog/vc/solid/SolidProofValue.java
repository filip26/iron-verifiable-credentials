package com.apicatalog.vc.solid;

import java.util.Objects;

import com.apicatalog.controller.key.RawKey;
import com.apicatalog.cryptosuite.CryptoSuite;
import com.apicatalog.cryptosuite.Signature;
import com.apicatalog.cryptosuite.VerificationError;
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
            RawKey publicKey) throws VerificationError {

        Objects.requireNonNull(data);
        Objects.requireNonNull(publicKey);
        Objects.requireNonNull(cryptoSuite);

        //TODO check key type
        
        final Signature signature = new Signature(cryptoSuite);

        // verify signature
        signature.verify(
                data,
                unsignedProof,
                publicKey.raw(),
                toByteArray);
    }
}
