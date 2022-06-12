package com.apicatalog.ld.signature.key;

import com.apicatalog.ld.signature.proof.VerificationMethod;

public interface VerificationKey extends VerificationMethod {

    byte[] getPublicKey();

}
