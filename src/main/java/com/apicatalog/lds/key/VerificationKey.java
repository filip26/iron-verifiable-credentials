package com.apicatalog.lds.key;

import com.apicatalog.lds.proof.VerificationMethod;

public interface VerificationKey extends VerificationMethod {

    byte[] getPublicKey();

}
