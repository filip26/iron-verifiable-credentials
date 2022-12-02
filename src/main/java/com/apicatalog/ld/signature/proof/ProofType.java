package com.apicatalog.ld.signature.proof;

import java.net.URI;

public interface ProofType {

    // proof type id
    URI id();

    // JSON-LD context defining the type
    URI context();

}
