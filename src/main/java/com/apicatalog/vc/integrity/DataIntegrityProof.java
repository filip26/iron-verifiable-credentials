package com.apicatalog.vc.integrity;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;

import com.apicatalog.jsonld.schema.LdObject;
import com.apicatalog.jsonld.schema.LdProperty;
import com.apicatalog.jsonld.schema.LdSchema;
import com.apicatalog.jsonld.schema.LdTerm;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.multibase.Multibase.Algorithm;
import com.apicatalog.vc.model.Proof;
import com.apicatalog.vc.suite.SignatureSuite;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

/**
 * Represents data integrity proof base class.
 *
 * @see <a href="https://www.w3.org/TR/vc-data-integrity/#proofs">Proofs</a>
 *
 */
public abstract  class DataIntegrityProof implements Proof {

    protected final SignatureSuite suite;
    protected final CryptoSuite crypto;

    final LdObject ldProof;
    final JsonObject expanded;

    final LdProperty<byte[]> proofValueSchema;
    
//    final LdSchema proofSchema = DataIntegritySchema.getProof(
//            LdTerm.create("TestSignatureSuite2022", "https://w3id.org/security#"),
//            DataIntegritySchema.getEmbeddedMethod(METHOD_SCHEMA),
//            PROOF_VALUE_PROPERTY);
//
    
    protected DataIntegrityProof(
            SignatureSuite suite, 
            CryptoSuite crypto,
            LdObject ldProof,
            JsonObject expandedProof,
            LdProperty<byte[]> proofValueSchema
            ) {
        this.suite = suite;
        this.crypto = crypto;
        this.ldProof = ldProof;
        this.expanded = expandedProof;
        this.proofValueSchema = proofValueSchema;
    }

    /**
     * The intent for the proof, the reason why an entity created it. Mandatory e.g.
     * assertion or authentication
     *
     * @see <a href=
     *      "https://w3c-ccg.github.io/data-integrity-spec/#proof-purposes">Proof
     *      Purposes</a>
     *
     * @return {@link URI} identifying the purpose
     */
    public URI getPurpose() {
        return ldProof.value(DataIntegritySchema.PURPOSE);
    }

    @Override
    public VerificationMethod getMethod() {
        return ldProof.value(DataIntegritySchema.VERIFICATION_METHOD);
    }

    /**
     * The string value of an ISO8601. Mandatory
     *
     * @return the date time when the proof has been created
     */
    public Instant getCreated() {
        return ldProof.value(DataIntegritySchema.CREATED);
    }

    /**
     * A string value specifying the restricted domain of the proof.
     *
     * @return the domain or <code>null</code>
     */
    public String getDomain() {
        return ldProof.value(DataIntegritySchema.DOMAIN);
    }

    /**
     * A string value used once for a particular domain and/or time. Used to
     * mitigate replay attacks.
     * 
     * @return the challenge or <code>null</code>
     */
    public String getChallenge() {
        return ldProof.value(DataIntegritySchema.CHALLENGE);
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
//        PROOF_SCHEMA.validate(ldProof, params);        
    }

//    @Override
//    public JsonObject removeProofValue(JsonObject expanded) {
//        return Json.createObjectBuilder(expanded).remove(DataIntegritySchema.PROOF_VALUE.uri()).build();
//    }
//
//    @Override
//    public JsonObject setProofValue(JsonObject expanded, byte[] proofValue) throws DocumentError {
//
//        final JsonValue value = proofValueSchema.write(proofValue);
//
//        return Json.createObjectBuilder(expanded).add(
//                DataIntegritySchema.PROOF_VALUE.uri(),
//                Json.createArrayBuilder().add(
//                        value))
//                .build();
//    }
    
    protected abstract JsonValue encodeProofValue(byte[] proofValue);
}
