package com.apicatalog.lds.proof;

import java.time.Instant;

//TODO make it class
public interface ProofOptions {

    VerificationMethod getVerificationMethod();

    Instant getCreated();

    String getDomain();

    String getType();
}
