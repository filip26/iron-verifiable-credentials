package com.apicatalog.vc;

import java.net.URI;

import com.apicatalog.jsonld.PropertyName;
import com.apicatalog.ld.signature.proof.ProofType;

public class TestProofType implements ProofType {

    static final String VOCAB = "https://example.org/security#";
    
    static final URI ID = URI.create(VOCAB + "TestSignatureSuite2022");
    
    static final URI CONTEXT = URI.create("classpath:data-integrity-test-signature-2022.jsonld");

    static final PropertyName PROOF_VALUE = PropertyName.create("proofValue", VOCAB);
    static final PropertyName PROOF_METHOD = PropertyName.create("verificationMethod", VOCAB);

    @Override
    public URI id() {
        return ID;
    }

    @Override
    public PropertyName proofValue() {
        return PROOF_VALUE;
    }

    @Override
    public PropertyName method() {
        return PROOF_METHOD;
    }

    @Override
    public URI context() {
        return CONTEXT;
    }
}
