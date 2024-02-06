package com.apicatalog.vc;

import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.vc.integrity.DataIntegrityProof;
import com.apicatalog.vc.integrity.DataIntegritySuite;

import jakarta.json.JsonObject;

class TestSignatureProof extends DataIntegrityProof {

    protected TestSignatureProof(DataIntegritySuite suite, CryptoSuite crypto, JsonObject expandedProof) {
        super(suite, crypto, expandedProof);
        // TODO Auto-generated constructor stub
    }

    

//    static final LdSchema METHOD_SCHEMA = DataIntegritySchema.getVerificationKey(
//            LdTerm.create("TestVerificationKey2022", "https://w3id.org/security#"),
//            DataIntegritySchema.getPublicKey(
//                    Algorithm.Base58Btc,
//                    // FIXME MulticodecRegistry.ED25519_PUBLIC_KEY
//                    (key) -> key == null || key.length > 0));

//    static final LdProperty<byte[]> PROOF_VALUE_PROPERTY = DataIntegritySchema.getProofValue(
//            Algorithm.Base58Btc,
//            key -> key.length == 32);

//    static final LdSchema PROOF_SCHEMA = DataIntegritySchema.getProof(
//            LdTerm.create("TestSignatureSuite2022", "https://w3id.org/security#"),
//            DataIntegritySchema.getEmbeddedMethod(METHOD_SCHEMA),
//            PROOF_VALUE_PROPERTY);

//    final CryptoSuite crypto;
//    final LdObject ldProof;
//    final JsonObject expanded;
//
//    TestSignatureProof(
//            CryptoSuite crypto,
//            LdObject ldProof,
//            JsonObject expanded
//
//    ) {
//        this.crypto = crypto;
//        this.ldProof = ldProof;
//        this.expanded = expanded;
//    }
//
//    public static final TestSignatureProof readProof(JsonObject expanded) throws DocumentError {
//
//        LdObject ldProof = DataIntegrityProofReader.read(expanded);
//
//        return new TestSignatureProof(CRYPTO, ldProof, expanded);
//    }
//
//    public static TestSignatureProof createDraft(
//            // proof options
//            VerificationMethod verificationMethod,
//            URI assertionMethod,
//            Instant created,
//            String domain,
//            String challenge) throws DocumentError {
//
//        Map<String, Object> rr = new HashMap<>();
//        rr.put(LdTerm.TYPE.uri(), URI.create(TestSignatureSuite.ID));
//
//        if (verificationMethod != null) {
//            rr.put(DataIntegritySchema.VERIFICATION_METHOD.uri(), verificationMethod);
//        }
//        if (created != null) {
//            rr.put(DataIntegritySchema.CREATED.uri(), created);
//        }
//        if (assertionMethod != null) {
//            rr.put(DataIntegritySchema.PURPOSE.uri(), assertionMethod);
//        }
//        if (domain != null) {
//            rr.put(DataIntegritySchema.DOMAIN.uri(), domain);
//        }
//        if (challenge != null) {
//            rr.put(DataIntegritySchema.CHALLENGE.uri(), challenge);
//        }
//
//        final LdObject ldProof = new LdObject(rr);
//
//        JsonObject expanded = PROOF_SCHEMA.write(ldProof);
//
//        return new TestSignatureProof(CRYPTO, ldProof, expanded);
//    }
//
//    @Override
//    public VerificationMethod getMethod() {
//        return ldProof.value(DataIntegritySchema.VERIFICATION_METHOD);
//    }
//
//    @Override
//    public byte[] getValue() {
//        return ldProof.value(DataIntegritySchema.PROOF_VALUE);
//    }
//
//    @Override
//    public URI id() {
//        return ldProof.value(LdTerm.ID);
//    }
//
//    @Override
//    public URI previousProof() {
//        return ldProof.value(DataIntegritySchema.PREVIOUS_PROOF);
//    }
//
//    @Override
//    public CryptoSuite getCryptoSuite() {
//        return crypto;
//    }
//
//    @Override
//    public JsonObject toJsonLd() {
//        return expanded;
//    }
//
//    @Override
//    public void validate(Map<String, Object> params) throws DocumentError {
//        PROOF_SCHEMA.validate(ldProof, params);
//    }
//
//    @Override
//    public ProofValueProcessor valueProcessor() {
//        return this;
//    }
//
//    @Override
//    public JsonObject removeProofValue(JsonObject expanded) {
//        return Json.createObjectBuilder(expanded).remove(DataIntegritySchema.PROOF_VALUE.uri()).build();
//    }
//
//    @Override
//    public JsonObject setProofValue(JsonObject expanded, byte[] proofValue) throws DocumentError {
//
//        final JsonValue value = PROOF_VALUE_PROPERTY.write(proofValue);
//
//        return Json.createObjectBuilder(expanded).add(
//                DataIntegritySchema.PROOF_VALUE.uri(),
//                Json.createArrayBuilder().add(
//                        value))
//                .build();
//    }
//
//    @Override
//    public MethodAdapter methodProcessor() {
//        return this;
//    }

//    @Override
//    public VerificationMethod read(JsonObject expanded) throws DocumentError {
//        return DataIntegritySchema.getEmbeddedMethod(METHOD_SCHEMA).read(expanded);
//    }

}
