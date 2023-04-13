package com.apicatalog.vc;

import java.net.URI;

import com.apicatalog.jsonld.schema.LdTerm;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.ld.signature.primitive.MessageDigest;
import com.apicatalog.ld.signature.primitive.Urdna2015;
import com.apicatalog.multibase.Multibase.Algorithm;
import com.apicatalog.multicodec.Multicodec.Codec;
import com.apicatalog.vc.integrity.DataIntegritySchema;
import com.apicatalog.vc.integrity.DataIntegritySuite;
import com.apicatalog.vc.model.Proof;
import com.apicatalog.vc.suite.SignatureSuite;

import jakarta.json.JsonObject;

class TestSignatureSuite implements SignatureSuite  {

    static final URI ID = URI.create(VcVocab.SECURITY_VOCAB + "TestSignatureSuite2022");

    static final URI CONTEXT = URI.create("classpath:data-integrity-test-signature-2022.jsonld");

    static final CryptoSuite CRYPTO = new CryptoSuite(
            "test-signature",
            new Urdna2015(),
            new MessageDigest("SHA-256"),
            new TestAlgorithm());

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
        
        
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public VerificationMethod readMethod(JsonObject expanded) throws DocumentError {
        // TODO Auto-generated method stub
        return null;
    }
}
