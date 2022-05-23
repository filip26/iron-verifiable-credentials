package com.apicatalog.vc;

public interface Verifier {

    void verify(Verifiable verifiable) throws VerificationError;

}
