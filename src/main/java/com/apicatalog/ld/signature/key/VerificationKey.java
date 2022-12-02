package com.apicatalog.ld.signature.key;

import com.apicatalog.ld.signature.method.VerificationMethod;

public interface VerificationKey extends VerificationMethod {

    byte[] publicKey();

}
