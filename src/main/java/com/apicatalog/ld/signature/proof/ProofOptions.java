package com.apicatalog.ld.signature.proof;

import com.apicatalog.ld.signature.SignatureSuite;

public interface ProofOptions {

    SignatureSuite getSuite();

    Proof toUnsignedProof();
}
