package com.apicatalog.ld.signature.proof;

import com.apicatalog.ld.schema.LdObject;
import com.apicatalog.ld.signature.SignatureSuite;

@Deprecated
public interface ProofOptions {

    SignatureSuite getSuite();

    LdObject toUnsignedProof();
}
