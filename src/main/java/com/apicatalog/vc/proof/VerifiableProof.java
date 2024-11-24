package com.apicatalog.vc.proof;

import java.util.Objects;

import com.apicatalog.controller.key.VerificationKey;
import com.apicatalog.cryptosuite.VerificationError;
import com.apicatalog.ld.DocumentError;

public interface VerifiableProof extends Proof {

    @Override
    default void verify(VerificationKey key) throws VerificationError, DocumentError {
        Objects.requireNonNull(key);
        // verify signature
        signature().verify(key);
    }
}
