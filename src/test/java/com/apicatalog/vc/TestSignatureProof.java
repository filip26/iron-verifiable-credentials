package com.apicatalog.vc;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.apicatalog.jsonld.schema.LdObject;
import com.apicatalog.jsonld.schema.LdProperty;
import com.apicatalog.jsonld.schema.LdSchema;
import com.apicatalog.jsonld.schema.LdTerm;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.ld.signature.primitive.MessageDigest;
import com.apicatalog.ld.signature.primitive.Urdna2015;
import com.apicatalog.multibase.Multibase.Algorithm;
import com.apicatalog.multicodec.Multicodec.Codec;
import com.apicatalog.vc.integrity.DataIntegritySchema;
import com.apicatalog.vc.model.Proof;
import com.apicatalog.vc.model.ProofValueProcessor;
import com.apicatalog.vc.suite.SignatureSuite;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

class TestSignatureProof implements Proof, ProofValueProcessor {

    static final CryptoSuite CRYPTO = new CryptoSuite(
            "test-signature",
            new Urdna2015(),
            new MessageDigest("SHA-256"),
            new TestAlgorithm());

    static final LdSchema METHOD_SCHEMA = DataIntegritySchema.getVerificationKey(
            LdTerm.create("TestVerificationKey2022", "https://w3id.org/security#"),
            DataIntegritySchema.getPublicKey(
                    Algorithm.Base58Btc,
                    Codec.Ed25519PublicKey,
                    (key) -> key == null || key.length > 0));

    static final LdProperty<byte[]> PROOF_VALUE_PROPERTY = DataIntegritySchema.getProofValue(
            Algorithm.Base58Btc,
            key -> key.length == 32);

    static final LdSchema PROOF_SCHEMA = DataIntegritySchema.getProof(
            LdTerm.create("TestSignatureSuite2022", "https://w3id.org/security#"),
            DataIntegritySchema.getEmbeddedMethod(METHOD_SCHEMA),
            PROOF_VALUE_PROPERTY);

    final SignatureSuite suite;
    final CryptoSuite crypto;
    final LdObject ldProof;
    final JsonObject expanded;
    

    TestSignatureProof(SignatureSuite suite,
            CryptoSuite crypto,
            LdObject ldProof,
            JsonObject expanded

    ) {
        this.suite = suite;
        this.crypto = crypto;
        this.ldProof = ldProof;
        this.expanded = expanded;
    }

    public static final TestSignatureProof read(SignatureSuite suite, JsonObject expanded) throws DocumentError {

        LdObject ldProof = PROOF_SCHEMA.read(expanded);

        return new TestSignatureProof(suite, CRYPTO, ldProof, expanded);
    }

    public static final VerificationMethod readMethod(SignatureSuite suite, JsonObject expanded) throws DocumentError {
        return DataIntegritySchema.getEmbeddedMethod(METHOD_SCHEMA).read(expanded);
    }

    public static TestSignatureProof createDraft(
            SignatureSuite suite,
            // proof options
            VerificationMethod verificationMethod,
            URI assertionMethod,
            Instant created,
            String domain) throws DocumentError {


        Map<String, Object> rr = new HashMap<>();
        rr.put(LdTerm.TYPE.uri(), URI.create(TestSignatureSuite.ID));   //TODO
        
        if (verificationMethod != null) {
            rr.put("https://w3id.org/security#verificationMethod", verificationMethod);
        }
        if (created != null) {
            rr.put("http://purl.org/dc/terms/created", created);
        }
        if (assertionMethod != null) {
            rr.put("https://w3id.org/security#proofPurpose", assertionMethod);
        }
        if (domain != null) {
            rr.put("https://w3id.org/security#domain", domain);
        }

        LdObject ldProof = new LdObject(rr);
System.out.println(ldProof);
        JsonObject expanded = PROOF_SCHEMA.write(ldProof);

        return new TestSignatureProof(suite, CRYPTO, ldProof, expanded);
        
//        Map<String, Object> proof = new LinkedHashMap<>();
//
//        proof.put(LdTerm.TYPE.uri(), type);
//        proof.put(DataIntegritySchema.CREATED.uri(), created);
//        proof.put(DataIntegritySchema.PURPOSE.uri(), purpose);
//        proof.put(DataIntegritySchema.VERIFICATION_METHOD.uri(), method);
//        proof.put(DataIntegritySchema.DOMAIN.uri(), domain);
//        proof.put(DataIntegritySchema.CHALLENGE.uri(), challenge);
//
//        return new LdObject(proof);        
    }

    @Override
    public VerificationMethod getMethod() {
        return ldProof.value(DataIntegritySchema.VERIFICATION_METHOD);
    }

    @Override
    public byte[] getValue() {
        return ldProof.value(DataIntegritySchema.PROOF_VALUE);
    }

    @Override
    public URI id() {
        return ldProof.value(LdTerm.ID);
    }

    @Override
    public URI previousProof() {
        return ldProof.value(DataIntegritySchema.PREVIOUS_PROOF);
    }

    @Override
    public CryptoSuite getCryptoSuite() {
        return crypto;
    }

    @Override
    public JsonObject toJsonLd() {
        return expanded;
    }

    @Override
    public void validate(Map<String, Object> params) throws DocumentError {
        PROOF_SCHEMA.validate(ldProof, params);
    }

//    @Override
//    public String getType() {
//        // TODO Auto-generated method stub
//        return null;
//    }

    @Override
    public SignatureSuite getSignatureSuite() {
        return suite;
    }

    @Override
    public ProofValueProcessor valueProcessor() {
        return this;
    }

    @Override
    public JsonObject removeProofValue(JsonObject expanded) {
        return Json.createObjectBuilder(expanded).remove(DataIntegritySchema.PROOF_VALUE.uri()).build();
    }

    @Override
    public JsonObject setProofValue(JsonObject expanded, byte[] proofValue) throws DocumentError {
        
      final JsonValue value = PROOF_VALUE_PROPERTY.write(proofValue);

      return Json.createObjectBuilder(expanded).add(
              DataIntegritySchema.PROOF_VALUE.uri(),
              Json.createArrayBuilder().add(
                      value))
              .build();
    }
}
