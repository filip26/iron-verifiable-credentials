package com.apicatalog.vc;

import java.net.URI;

import com.apicatalog.ld.schema.LdTerm;
import com.apicatalog.ld.signature.proof.ProofType;

public class TestProofType implements ProofType {

    static final String VOCAB = "https://example.org/security#";
    
    static final URI ID = URI.create(VOCAB + "TestSignatureSuite2022");
    
    static final URI CONTEXT = URI.create("classpath:data-integrity-test-signature-2022.jsonld");

    static final LdTerm PROOF_VALUE = LdTerm.create("proofValue", VOCAB);
    static final LdTerm PROOF_METHOD = LdTerm.create("verificationMethod", VOCAB);

    @Override
    public URI id() {
        return ID;
    }

//    @Override
//    public LdTerm proofValue() {
//        return PROOF_VALUE;
//    }
//
//    @Override
//    public LdTerm method() {
//        return PROOF_METHOD;
//    }

    @Override
    public URI context() {
        return CONTEXT;
    }
}
