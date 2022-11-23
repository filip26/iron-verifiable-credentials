package com.apicatalog.vc.processor;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.ld.DocumentError;

public interface StatusVerifier {

    public interface Status {
        
        URI getId();
        
        Collection<String> getType();
    }

    void verify(Status status) throws DocumentError, VerifyError;
}