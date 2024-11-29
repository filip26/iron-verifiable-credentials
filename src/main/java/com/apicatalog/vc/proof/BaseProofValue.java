package com.apicatalog.vc.proof;

import java.util.Collection;

import com.apicatalog.controller.key.VerificationKey;
import com.apicatalog.cryptosuite.CryptoSuiteError;
import com.apicatalog.cryptosuite.VerificationError;
import com.apicatalog.cryptosuite.VerificationError.VerificationErrorCode;
import com.apicatalog.vc.model.DocumentError;

public interface BaseProofValue extends ProofValue {

    /**
     * Base proof value cannot be verified. It's used to create a derived proof
     * value. Must throw {@link VerificationError}.
     * 
     * @param publicKey
     * 
     * @throws VerificationError
     * 
     */
    @Override
    default void verify(VerificationKey publicKey) throws VerificationError {
        throw new VerificationError(VerificationErrorCode.InvalidSignature);
    }

    /**
     * Derive a new selective disclosure proof value from this base proof value.
     * 
     * @param selectors
     * 
     * @return a new derived proof value
     * 
     * @throws CryptoSuiteError
     * @throws DocumentError
     */
    DerivedProofValue derive(Collection<String> selectors) throws CryptoSuiteError, DocumentError;

    Collection<String> pointers();

//    VerifiableMaterial document();
//
//    VerifiableMaterial proof();

}
