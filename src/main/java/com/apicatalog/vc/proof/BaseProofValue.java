package com.apicatalog.vc.proof;

import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.VerificationError.Code;

import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;

public interface BaseProofValue extends ProofValue {

    @Override
    default void verify(CryptoSuite crypto, JsonStructure context, JsonObject data, JsonObject unsignedProof, byte[] publicKey) throws VerificationError, DocumentError {
        throw new VerificationError(Code.InvalidSignature);
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
