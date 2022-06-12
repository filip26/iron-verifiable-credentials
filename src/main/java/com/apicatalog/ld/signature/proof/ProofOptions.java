package com.apicatalog.ld.signature.proof;

import java.net.URI;
import java.time.Instant;

//TODO make it class
public interface ProofOptions {

    VerificationMethod getVerificationMethod();

    Instant getCreated();

    String getDomain();

    String getType();
    
    URI getPurpose();
}
