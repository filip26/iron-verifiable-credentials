package com.apicatalog.ld.signature.key;

import com.apicatalog.vc.method.VerificationMethod;

public interface VerificationKey extends VerificationMethod {

    byte[] publicKey();

}
