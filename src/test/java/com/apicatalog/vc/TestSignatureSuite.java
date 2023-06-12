package com.apicatalog.vc;

import java.net.URI;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.vc.model.Proof;
import com.apicatalog.vc.suite.SignatureSuite;

import jakarta.json.JsonObject;

class TestSignatureSuite implements SignatureSuite  {

    static final URI ID = URI.create(VcVocab.SECURITY_VOCAB + "TestSignatureSuite2022");

    static final URI CONTEXT = URI.create("classpath:data-integrity-test-signature-2022.jsonld");

    public TestSignatureSuite() {
//        super(ID, CONTEXT, CRYPTO, DataIntegritySchema.getProof(
//                LdTerm.create("TestSignatureSuite2022", "https://w3id.org/security#"),
//                Algorithm.Base58Btc,
//                key -> key.length == 32,
//                DataIntegritySchema.getVerificationKey(
//                        LdTerm.create("TestVerificationKey2022", "https://w3id.org/security#"),
//                        DataIntegritySchema.getPublicKey(
//                                Algorithm.Base58Btc,
//                                Codec.Ed25519PublicKey,
//                                (key) -> key == null || key.length > 0
//                        )
//                )));
    }

    @Override
    public URI id() {
        return ID;
    }

    @Override
    public URI context() {
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
