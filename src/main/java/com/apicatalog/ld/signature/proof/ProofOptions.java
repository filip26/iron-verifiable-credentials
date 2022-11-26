package com.apicatalog.ld.signature.proof;

import com.apicatalog.ld.signature.SignatureSuite;
import com.apicatalog.ld.signature.method.VerificationMethod;

public interface ProofOptions {

    SignatureSuite getSuite();
    
    VerificationMethod getMethod();
}
