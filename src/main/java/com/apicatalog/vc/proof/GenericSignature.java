package com.apicatalog.vc.proof;

import com.apicatalog.controller.key.VerificationKey;
import com.apicatalog.cryptosuite.VerificationError;
import com.apicatalog.cryptosuite.VerificationError.VerificationErrorCode;
import com.apicatalog.ld.DocumentError;

public class GenericSignature implements ProofValue {

    @Override
    public void verify(VerificationKey key) throws VerificationError, DocumentError {
        throw new VerificationError(VerificationErrorCode.UnsupportedSignature);
    }

}
