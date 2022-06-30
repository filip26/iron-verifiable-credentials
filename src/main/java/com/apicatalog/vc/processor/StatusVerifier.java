package com.apicatalog.vc.processor;

import java.net.URI;

import com.apicatalog.ld.signature.DataError;

public interface StatusVerifier {

    void verify(Status status) throws DataError, VerifyError;

    public interface Status {
        URI getId();
        String getType();
    }
    
}
