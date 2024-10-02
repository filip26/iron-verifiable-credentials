package com.apicatalog.vc.proof;

import java.util.Collection;

import com.apicatalog.controller.key.RawKey;
import com.apicatalog.cryptosuite.CryptoSuite;
import com.apicatalog.cryptosuite.SigningError;
import com.apicatalog.cryptosuite.VerificationError;
import com.apicatalog.cryptosuite.VerificationError.VerificationErrorCode;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.LinkedTree;

import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;

public interface BaseProofValue extends ProofValue {

    @Override
    default void verify(CryptoSuite crypto, LinkedTree data, LinkedTree unsignedProof, RawKey publicKey) throws VerificationError, DocumentError {
        throw new VerificationError(VerificationErrorCode.InvalidSignature);
    }

    /**
     * Derives a new selective disclosure proof from a base proof. 
     * 
     * @param context
     * @param data
     * @param selectors
     * @return
     * @throws SigningError
     * @throws DocumentError 
     * @throws {@link UnsupportedOperationException} if the suite does not support selective disclosure
     */
    ProofValue derive(JsonStructure context, JsonObject data, Collection<String> selectors) throws SigningError, DocumentError;

    Collection<String> pointers();
}
