package com.apicatalog.vc.proof;

import com.apicatalog.controller.key.VerificationKey;
import com.apicatalog.cryptosuite.VerificationError;
import com.apicatalog.cryptosuite.VerificationError.VerificationErrorCode;
import com.apicatalog.vc.model.DocumentError;

public class GenericSignature implements ProofValue {

    protected final Proof proof;
    
    public GenericSignature(final Proof proof) {
        this.proof = proof;
    }
    
    @Override
    public void verify(VerificationKey key) throws VerificationError, DocumentError {
        throw new VerificationError(VerificationErrorCode.UnsupportedSignature);
    }

    @Override
    public Proof proof() {
        return proof;
    }

}
