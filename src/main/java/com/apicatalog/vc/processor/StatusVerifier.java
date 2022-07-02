package com.apicatalog.vc.processor;

import java.net.URI;

import com.apicatalog.ld.DocumentError;

public interface StatusVerifier {

    public interface Status {
        URI getId();
        String getType();
    }

    void verify(Status status) throws DocumentError, VerifyError;
}