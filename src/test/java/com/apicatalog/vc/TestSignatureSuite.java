package com.apicatalog.vc;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.vc.model.Proof;
import com.apicatalog.vc.suite.SignatureSuite;

import jakarta.json.JsonObject;

class TestSignatureSuite implements SignatureSuite  {

    static final String ID = VcVocab.SECURITY_VOCAB + "TestSignatureSuite2022";

    static final String CONTEXT = "classpath:data-integrity-test-signature-2022.jsonld";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String context() {
        return CONTEXT;
    }

    @Override
    public Proof readProof(JsonObject expanded) throws DocumentError {
        return TestSignatureProof.read(this, expanded);
    }

    @Override
    public VerificationMethod readMethod(JsonObject expanded) throws DocumentError {
        return TestSignatureProof.readMethod(this, expanded);
    }
}
