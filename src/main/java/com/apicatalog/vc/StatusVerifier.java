package com.apicatalog.vc;

import com.apicatalog.lds.DataError;

public interface StatusVerifier {

    void verify(CredentialStatus status) throws DataError, VerifyError;
    
}
