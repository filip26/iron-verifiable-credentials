package com.apicatalog.vc;

import com.apicatalog.ld.signature.DataError;

public interface StatusVerifier {

    void verify(CredentialStatus status) throws DataError, VerifyError;

}
