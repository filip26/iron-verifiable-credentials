package com.apicatalog.vc;

import com.apicatalog.jsonld.loader.DocumentLoader;

public interface VerificationApi {

    VerificationApi loader(DocumentLoader loader);
    
    void verify();
}
