package com.apicatalog.ld.signature.proof;

import java.net.URI;

import com.apicatalog.ld.schema.LdTerm;

public interface ProofType {

    // proof type id
    URI id();   

    // JSON-LD context defining the type
    URI context();
  
}
