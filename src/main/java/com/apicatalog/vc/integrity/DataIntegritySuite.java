package com.apicatalog.vc.integrity;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.schema.LdObject;
import com.apicatalog.jsonld.schema.LdProperty;
import com.apicatalog.jsonld.schema.LdSchema;
import com.apicatalog.jsonld.schema.LdTerm;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.vc.VcVocab;
import com.apicatalog.vc.model.Proof;
import com.apicatalog.vc.suite.SignatureSuite;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public class DataIntegritySuite implements SignatureSuite {

    protected static final String PROOF_TYPE_NAME = "DataIntegrityProof";

    protected static final String PROOF_TYPE_ID = VcVocab.SECURITY_VOCAB + PROOF_TYPE_NAME;

    protected final LdSchema methodSchema;
    protected final LdProperty<byte[]> proofValueSchema;

    protected final CryptoSuite cryptosuite;

    protected DataIntegritySuite(CryptoSuite cryptosuite, final LdSchema methodSchema, final LdProperty<byte[]> proofValueSchema) {
        this.cryptosuite = cryptosuite;
        this.methodSchema = methodSchema;
        this.proofValueSchema = proofValueSchema;
    }

    @Override
    public boolean isSupported(String proofType, JsonObject expandedProof) {
        return PROOF_TYPE_ID.equals(proofType) && cryptosuite.getId().equals(getCryptoSuiteName(expandedProof));
    }

    static String getCryptoSuiteName(final JsonObject expandedProof) {
        if (expandedProof == null) {
            throw new IllegalArgumentException("expandedProof property must not be null.");
        }

        final JsonArray cryptos = expandedProof.getJsonArray(DataIntegritySchema.CRYPTO_SUITE.uri());

        for (final JsonValue valueObject : cryptos) {
            if (JsonUtils.isObject(valueObject) && valueObject.asJsonObject().containsKey(Keywords.VALUE)) {

                final JsonValue value = valueObject.asJsonObject().get(Keywords.VALUE);

                if (JsonUtils.isString(value)) {
                    return ((JsonString) value).getString();
                }
            }
        }

        return null;
    }

    @Override
    public Proof readProof(JsonObject expanded) throws DocumentError {

        final LdSchema proofSchema = DataIntegritySchema.getProof(
                LdTerm.create(PROOF_TYPE_NAME, VcVocab.SECURITY_VOCAB),
                DataIntegritySchema.getEmbeddedMethod(methodSchema),
                proofValueSchema);

        LdObject ldProof = proofSchema.read(expanded);

        return new DataIntegrityProof(this, proofSchema, ldProof, expanded);
    }

    public DataIntegrityProof createDraft(
            VerificationMethod method,
            URI purpose,
            Instant created) throws DocumentError {
        return createDraft(method, purpose, created, null, null);
    }

    public DataIntegrityProof createDraft(
            VerificationMethod method,
            URI purpose,
            Instant created,
            String domain) throws DocumentError {
        return createDraft(method, purpose, created, domain, null);
    }

    public DataIntegrityProof createDraft(
            VerificationMethod method,
            URI purpose,
            Instant created,
            String domain,
            String challenge) throws DocumentError {

        Map<String, Object> proof = new LinkedHashMap<>();

        proof.put(LdTerm.TYPE.uri(), URI.create(PROOF_TYPE_ID));
        proof.put(DataIntegritySchema.CRYPTO_SUITE.uri(), cryptosuite.getId());
        proof.put(DataIntegritySchema.CREATED.uri(), created);
        proof.put(DataIntegritySchema.PURPOSE.uri(), purpose);
        proof.put(DataIntegritySchema.VERIFICATION_METHOD.uri(), method);
        if (domain != null) {
            proof.put(DataIntegritySchema.DOMAIN.uri(), domain);
        }
        if (challenge != null) {
            proof.put(DataIntegritySchema.CHALLENGE.uri(), challenge);
        }

        final LdObject ldProof = new LdObject(proof);

        final LdSchema proofSchema = DataIntegritySchema.getProof(
                LdTerm.create(PROOF_TYPE_NAME, VcVocab.SECURITY_VOCAB),
                DataIntegritySchema.getEmbeddedMethod(methodSchema),
                proofValueSchema);

        final JsonObject expanded = proofSchema.write(ldProof);

        return new DataIntegrityProof(this, proofSchema, ldProof, expanded);
    }
}
