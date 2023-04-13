package com.apicatalog.vc;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;

import com.apicatalog.jsonld.schema.LdTerm;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.ld.signature.primitive.MessageDigest;
import com.apicatalog.ld.signature.primitive.Urdna2015;
import com.apicatalog.multibase.Multibase.Algorithm;
import com.apicatalog.multicodec.Multicodec.Codec;
import com.apicatalog.vc.integrity.DataIntegritySchema;
import com.apicatalog.vc.integrity.DataIntegrityProof;
import com.apicatalog.vc.integrity.DataIntegritySuite;
import com.apicatalog.vc.model.Proof;
import com.apicatalog.vc.suite.SignatureSuite;

import jakarta.json.JsonObject;

class TestSignatureProof implements Proof {

    static final LdTerm ID = LdTerm.create("TestSignatureSuite2022", VcVocab.SECURITY_VOCAB);

    static final URI CONTEXT = URI.create("classpath:data-integrity-test-signature-2022.jsonld");

//    static final CryptoSuite CRYPTO = new CryptoSuite(
//            ID,
//            new Urdna2015(),
//            new MessageDigest("SHA-256"),
//            new TestAlgorithm());

    public TestSignatureProof(VerificationMethod verificationMethod, 
            URI purpose, 
            Instant created, 
            String domain) {
        
//        super(ID.uri());
        
//        super(ID, CONTEXT, CRYPTO, DataIntegrity.getProof(
//                LdTerm.create("TestSignatureSuite2022", "https://w3id.org/security#"),
//                Algorithm.Base58Btc,
//                key -> key.length == 32,
//                DataIntegrity.getVerificationKey(
//                        LdTerm.create("TestVerificationKey2022", "https://w3id.org/security#"),
//                        DataIntegrity.getPublicKey(
//                                Algorithm.Base58Btc,
//                                Codec.Ed25519PublicKey,
//                                (key) -> key == null || key.length > 0
//                        )
//                )));
    }

    @Override
    public Collection<VerificationMethod> getMethod() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] getValue() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public URI id() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public URI previousProof() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CryptoSuite getCryptoSuite() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SignatureSuite getSignatureSuite() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JsonObject toJsonLd() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void validate(Map<String, Object> params) throws DocumentError {
        // TODO Auto-generated method stub
        
    }

    @Override
    public JsonObject removeProofValue(JsonObject expanded) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JsonObject setProofValue(JsonObject expanded, byte[] proofValue) {
        // TODO Auto-generated method stub
        return null;
    }
}
