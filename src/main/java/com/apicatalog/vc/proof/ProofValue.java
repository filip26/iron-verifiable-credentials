package com.apicatalog.vc.proof;

import com.apicatalog.controller.key.VerificationKey;
import com.apicatalog.cryptosuite.VerificationError;
import com.apicatalog.ld.DocumentError;

public interface ProofValue {

    void verify(VerificationKey key) throws VerificationError, DocumentError;

}
