package com.apicatalog.vc;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.model.Proof;
import com.apicatalog.vc.suite.SignatureSuite;

import jakarta.json.JsonObject;

class TestSignatureSuite implements SignatureSuite  {

    static final String ID = VcVocab.SECURITY_VOCAB + "TestSignatureSuite2022";

    static final String CONTEXT = "classpath:data-integrity-test-signature-2022.jsonld";

    @Override
    public Proof readProof(JsonObject proof) throws DocumentError {
        return TestSignatureProof.readProof(proof);
    }

    @Override
    public boolean isSupported(String proofType, JsonObject proof) {
        return ID.equals(proofType);
    }
}
