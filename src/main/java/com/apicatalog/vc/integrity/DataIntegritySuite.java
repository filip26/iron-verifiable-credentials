package com.apicatalog.vc.integrity;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import com.apicatalog.jsonld.schema.LdObject;
import com.apicatalog.jsonld.schema.LdProperty;
import com.apicatalog.jsonld.schema.LdSchema;
import com.apicatalog.jsonld.schema.LdTerm;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.node.LdAdapter;
import com.apicatalog.ld.node.LdNode;
import com.apicatalog.ld.node.LdNodeBuilder;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.vc.VcVocab;
import com.apicatalog.vc.method.MethodAdapter;
import com.apicatalog.vc.model.Proof;
import com.apicatalog.vc.suite.SignatureSuite;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;

public abstract class DataIntegritySuite implements SignatureSuite {

    protected static final String PROOF_TYPE_NAME = "DataIntegrityProof";

    protected static final String PROOF_TYPE_ID = VcVocab.SECURITY_VOCAB + PROOF_TYPE_NAME;

//    protected final LdSchema methodSchema;
    protected final MethodAdapter method;
//    protected final LdProperty<byte[]> proofValueSchema;
    protected final LdAdapter<byte[]> proofValue;

    protected final String cryptosuite;

    protected DataIntegritySuite(String cryptosuite, final MethodAdapter method, final LdAdapter<byte[]> proofValue) {
        this.cryptosuite = cryptosuite;
        this.method = method;
        this.proofValue = proofValue;
    }

    @Override
    public boolean isSupported(String proofType, JsonObject expandedProof) {
        return PROOF_TYPE_ID.equals(proofType) && cryptosuite.equals(getCryptoSuiteName(expandedProof));
    }

    static String getCryptoSuiteName(final JsonObject proof) {
        if (proof == null) {
            throw new IllegalArgumentException("expandedProof property must not be null.");
        }

        try {
        
        return LdNode.get(proof, DataIntegrityVocab.CRYPTO_SUITE).required()
                .scalar().string();

        } catch (DocumentError e) {
            
        }
        return null;
//        
//        final JsonArray cryptos = proof.getJsonArray(DataIntegritySchema.CRYPTO_SUITE.uri());
//
//        for (final JsonValue valueObject : cryptos) {
//            if (JsonUtils.isObject(valueObject) && valueObject.asJsonObject().containsKey(Keywords.VALUE)) {
//
//                final JsonValue value = valueObject.asJsonObject().get(Keywords.VALUE);
//
//                if (JsonUtils.isString(value)) {
//                    return ((JsonString) value).getString();
//                }
//            }
//        }

//        return null;
    }

    @Override
    public Proof readProof(JsonObject proof) throws DocumentError {

//        final LdSchema proofSchema = DataIntegritySchema.getProof(
//                LdTerm.create(PROOF_TYPE_NAME, VcVocab.SECURITY_VOCAB),
//                DataIntegritySchema.getEmbeddedMethod(methodSchema),
//                proofValueSchema);
//
//        final LdObject ldProof = proofSchema.read(expanded);

        return DataIntegrityProofReader.read(proof, this, method, proofValue);
    }

    protected abstract CryptoSuite getCryptoSuite(String cryptoName) throws DocumentError;
    
    protected DataIntegrityProof createDraft(
            CryptoSuite crypto,
            VerificationMethod method,
            URI purpose,
            Instant created,
            String domain,
            String challenge) throws DocumentError {

//        Map<String, Object> proof = new LinkedHashMap<>();
////
//        final LdObject ldProof = new LdObject(proof);
//
//        final LdSchema proofSchema = DataIntegritySchema.getProof(
//                LdTerm.create(PROOF_TYPE_NAME, VcVocab.SECURITY_VOCAB),
//                DataIntegritySchema.getEmbeddedMethod(methodSchema),
//                proofValueSchema);

        final LdNodeBuilder builder = new LdNodeBuilder();
 
        builder.type(PROOF_TYPE_ID);
        builder.set(DataIntegrityVocab.CRYPTO_SUITE).string(cryptosuite);
        
        builder.set(DataIntegrityVocab.CREATED).xsdDateTime(created);
        builder.set(DataIntegrityVocab.PURPOSE).link(purpose);

        if (domain != null) {
            builder.set(DataIntegrityVocab.DOMAIN).string(domain);
        }
        if (challenge != null) {
            builder.set(DataIntegrityVocab.CHALLENGE).string(challenge);
        }

        final DataIntegrityProof proof = new DataIntegrityProof(this, crypto, builder.build());
        proof.created = created;
        proof.purpose = purpose;
        proof.method = method;
        proof.domain = domain;
        proof.challenge = challenge;

//      proof.put(DataIntegritySchema.VERIFICATION_METHOD.uri(), method);
      
        
//        return DataIntegrityProofReader.read(proof, this, getCryptoSuite(proof));
//        return new DataIntegrityProof(this, crypto, proofSchema, ldProof, expanded);
        //FIXME
        return proof;
    }
}
