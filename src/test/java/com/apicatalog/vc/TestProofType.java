package com.apicatalog.vc;

import java.net.URI;

import com.apicatalog.ld.signature.proof.ProofType;

public class TestProofType implements ProofType {

    static final String VOCAB = "https://w3id.org/security#";

    static final URI ID = URI.create(VOCAB + "TestSignatureSuite2022");

    static final URI CONTEXT = URI.create("classpath:data-integrity-test-signature-2022.jsonld");

    @Override
    public URI id() {
        return ID;
    }

    @Override
    public URI context() {
        return CONTEXT;
    }
}
