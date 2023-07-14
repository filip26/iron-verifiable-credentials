package com.apicatalog.vc.integrity;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.schema.LdObject;
import com.apicatalog.jsonld.schema.LdProperty;
import com.apicatalog.jsonld.schema.LdSchema;
import com.apicatalog.jsonld.schema.LdTerm;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.vc.VcVocab;
import com.apicatalog.vc.model.Proof;
import com.apicatalog.vc.suite.SignatureSuite;

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

    static String getCryptoSuiteName(JsonObject expandedProof) {
        if (expandedProof == null) {
            throw new IllegalArgumentException("expandedProof property must not be null.");
        }

        JsonValue value = expandedProof.get(DataIntegritySchema.CRYPTO_SUITE.uri());

        if (JsonUtils.isString(value)) {
            return ((JsonString) value).getString();
        }

        return null;
    }

    @Override
    public Proof readProof(JsonObject expanded) throws DocumentError {

        final LdSchema proofSchema = DataIntegritySchema.getProof(
                LdTerm.create(PROOF_TYPE_NAME, VcVocab.SECURITY_VOCAB ),
                DataIntegritySchema.getEmbeddedMethod(methodSchema),
                proofValueSchema);

        LdObject ldProof = proofSchema.read(expanded);

        return new DataIntegrityProof(this, proofSchema, ldProof, expanded);
    }
}
