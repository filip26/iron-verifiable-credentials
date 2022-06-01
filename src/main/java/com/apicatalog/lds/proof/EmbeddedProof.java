package com.apicatalog.lds.proof;

import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.lang.NodeObject;
import com.apicatalog.jsonld.lang.ValueObject;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.lds.DataIntegrityError;
import com.apicatalog.lds.DataIntegrityError.Code;
import com.apicatalog.lds.VerificationError;
import com.apicatalog.lds.ed25519.Ed25519KeyPair2020;
import com.apicatalog.multibase.Multibase;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

/**
 * An embedded proof is included in the data, such as a Linked Data Signature.
 */
public class EmbeddedProof implements Proof {

    public static final String CREATED = "http://purl.org/dc/terms/created";
    public static final String PROOF = "https://w3id.org/security#proof";
    public static final String PROOF_PURPOSE = "https://w3id.org/security#proofPurpose";
    public static final String PROOF_VERIFICATION_METHOD = "https://w3id.org/security#verificationMethod";
    public static final String PROOF_DOMAIN = "https://w3id.org/security#domain";
    public static final String PROOF_VALUE = "https://w3id.org/security#proofValue";
    
    private String type;

    private String purpose;

    private VerificationMethod verificationMethod;
    
    private Instant created;
    
    private String domain;
    
    private byte[] value;    

    public static EmbeddedProof from(ProofOptions options) {
        final EmbeddedProof proof = new EmbeddedProof();

        proof.type = options.getType();
        proof.verificationMethod = options.getVerificationMethod();
        proof.created = options.getCreated();
        proof.domain = options.getDomain();
        
        return proof;
    }
    
    /**
     * 
     * @param json expanded verifiable credentials or presentation
     * @param result
     * @return
     * @throws VerificationError
     */
    public static EmbeddedProof from(final JsonObject json, final DocumentLoader loader) throws DataIntegrityError {

        if (json == null) {
            throw new IllegalArgumentException("Parameter 'json' must not be null.");
        }

        final JsonValue proofValue = json.get(PROOF); //TODO move out

        if (proofValue == null) {
            throw new DataIntegrityError(Code.MissingProof);
        }

        if (!ValueType.ARRAY.equals(proofValue.getValueType())) {
            throw new DataIntegrityError();
        }
        
        for (JsonValue proofItem : proofValue.asJsonArray()) {
            
            // data integrity checks
            if (JsonUtils.isNotObject(proofItem)) {
                throw new DataIntegrityError(Code.InvalidProof);
            }

            if (proofItem.asJsonObject().containsKey(Keywords.GRAPH)) { //TODO hack
                proofItem = proofItem.asJsonObject().get(Keywords.GRAPH);
                if (JsonUtils.isArray(proofItem)) {
                    proofItem = proofItem.asJsonArray().get(0); //FIXME !?!
                }
                if (JsonUtils.isNotObject(proofItem)) {
                    throw new DataIntegrityError(Code.InvalidProof);
                }                
            }

            final JsonObject proofObject = proofItem.asJsonObject();
            
            final EmbeddedProof embeddedProof = new EmbeddedProof();

            // @type property
            if (!proofObject.containsKey(Keywords.TYPE)) {
                throw new DataIntegrityError(Code.MissingProofType);
            }

            final JsonValue typeValue = proofObject.get(Keywords.TYPE);
            
            if (JsonUtils.isArray(typeValue)) {
                
                // all @type values must be string
                if (!typeValue.asJsonArray().stream().allMatch(JsonUtils::isString)) {
                    throw new DataIntegrityError(Code.InvalidProofType);
                }
                
                embeddedProof.type = typeValue.asJsonArray().stream()
                                        .map(JsonString.class::cast)
                                        .map(JsonString::getString)
                                        .findAny().orElse(null);
                
            } else if (JsonUtils.isString(typeValue)) {
                embeddedProof.type = ((JsonString)typeValue).getString();
                
            } else {
                throw new DataIntegrityError(Code.InvalidProofType);
            }

            // proofPurpose property
            if (!proofObject.containsKey(PROOF_PURPOSE)) {
                throw new DataIntegrityError(Code.MissingProofPurpose);
            }

            final JsonValue proofPurposeValue = proofObject.get(PROOF_PURPOSE);
            
            if (JsonUtils.isArray(proofPurposeValue)) {
                 
                if (!proofPurposeValue.asJsonArray().stream().allMatch(NodeObject::isNodeReference)) {
                    throw new DataIntegrityError(Code.InvalidProofPurpose);
                }
                
                embeddedProof.purpose = proofPurposeValue.asJsonArray().stream()
                                        .map(JsonValue::asJsonObject)
                                        .map(o -> o.getString(Keywords.ID))
                                        .limit(1).toArray(String[]::new)[0];
            } else {
                throw new DataIntegrityError(Code.InvalidProofPurpose);
            }

            // verificationMethod property
            if (!proofObject.containsKey(PROOF_VERIFICATION_METHOD)) {
                throw new DataIntegrityError(Code.MissingVerificationMethod);
            }

            final JsonValue verificationMethodValue = proofObject.get(PROOF_VERIFICATION_METHOD);
            
            if (JsonUtils.isArray(verificationMethodValue) && verificationMethodValue.asJsonArray().size() > 0) {

                final JsonValue verificationMethodItem = verificationMethodValue.asJsonArray().get(0);
                                
                if (NodeObject.isNodeReference(verificationMethodItem)) { 

                    final JsonObject verificationMethodObject = verificationMethodItem.asJsonObject();

                    final String id = verificationMethodObject.getString(Keywords.ID);
                    
                    embeddedProof.verificationMethod = Ed25519KeyPair2020.reference(URI.create(id));
                    
                //TODO embedded key
                    
                } else {
                    throw new DataIntegrityError(Code.InvalidVerificationMethod);
                }
                
                
            } else {
                throw new DataIntegrityError(Code.InvalidVerificationMethod);
            }

            // proofValue property
            if (!proofObject.containsKey(PROOF_VALUE)) {
                throw new DataIntegrityError(Code.MissingProofValue);
            }

            final JsonValue embeddedProofValue = proofObject.get(PROOF_VALUE);
            
            if (JsonUtils.isArray(embeddedProofValue)) {
                 
                if (!embeddedProofValue.asJsonArray().stream().allMatch(ValueObject::isValueObject)
                        || !embeddedProofValue.asJsonArray().stream()
                                .map(JsonValue::asJsonObject)
                                .map(o -> o.get(Keywords.VALUE))
                                .allMatch(JsonUtils::isString)
                        ) {
                    throw new DataIntegrityError(Code.InvalidProofValue);
                }
                

                String proofValueType = embeddedProofValue.asJsonArray().getJsonObject(0).getString(Keywords.TYPE);
                                
                String encodedProofValue =  embeddedProofValue.asJsonArray().getJsonObject(0).getString(Keywords.VALUE);
                
                // verify supported proof value encoding
                if (!"https://w3id.org/security#multibase".equals(proofValueType)) {
                    throw new DataIntegrityError(Code.InvalidProofValue);        //FIXME belongs to ED25...
                }

                // verify proof value
                if (encodedProofValue == null || !Multibase.isAlgorithmSupported(encodedProofValue)) {
                    throw new DataIntegrityError(Code.InvalidProofValue);
                }

                // decode proof value
                byte[] rawProofValue = Multibase.decode(encodedProofValue);
              
                // verify proof value length
                if (rawProofValue.length != 64) {
                    throw new DataIntegrityError(Code.InvalidProofValueLength);
                }
                
                embeddedProof.value = rawProofValue;
                
            } else {
                throw new DataIntegrityError(Code.InvalidProofValue);
            }
            
            // created property
            if (!proofObject.containsKey(CREATED)) {
                throw new DataIntegrityError(Code.MissingCreated);
            }

            final JsonValue createdValue = proofObject.get(CREATED);
            
            if (JsonUtils.isArray(createdValue)) {

                // take first created property
                final JsonValue createdItem = createdValue.asJsonArray().get(0);
                
                // expect value object and date in ISO 8601 format
                if (!ValueObject.isValueObject(createdItem)) {
                    throw new DataIntegrityError(Code.InvalidCreated);
                }

                //TODO check @type
                
                String createdString = ValueObject.getValue(createdItem).filter(JsonUtils::isString)
                .map(JsonString.class::cast)
                .map(JsonString::getString).orElseThrow(DataIntegrityError::new);

                try {
                    OffsetDateTime created = OffsetDateTime.parse(createdString);
                    
                    embeddedProof.created = created.toInstant(); 
                
                } catch (DateTimeParseException e) {
                    throw new DataIntegrityError(Code.InvalidCreated);
                }
                

            } else {
                throw new DataIntegrityError(Code.InvalidCreated);
            }

            
            //TODO domain property
            


            return embeddedProof;       //FIXME process other proofs
        }
        



        //TODO
        return null;
    }    
    
    @Override
    public String getType() {
        return type;
    }


    @Override
    public String getPurpose() {
        return purpose;
    }

    @Override
    public VerificationMethod getVerificationMethod() {
        return verificationMethod;
    }

    @Override
    public Instant getCreated() {
        return created;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public byte[] getValue() {
        return value;
    }

    public JsonObject toJson() {
        return Json.createObjectBuilder().add(Keywords.TYPE, Json.createArrayBuilder().add(type))

                .add(PROOF_VERIFICATION_METHOD,
                        Json.createArrayBuilder()
                                .add(Json.createObjectBuilder().add(Keywords.ID,
                                        verificationMethod.getId().toString())))
                .add(CREATED,
                        Json.createArrayBuilder()
                                .add(Json.createObjectBuilder()
                                        .add(Keywords.TYPE, "http://www.w3.org/2001/XMLSchema#dateTime")
                                        .add(Keywords.VALUE, created.toString())

                                ))
                .add(PROOF_PURPOSE,
                        Json.createArrayBuilder()
                                .add(Json.createObjectBuilder().add(Keywords.ID,
                                        "https://w3id.org/security#assertionMethod")))  //FIXME configurable
                // TODO domain to proof

                .build();
    }

    public static JsonObject setProof(JsonObject document, JsonObject proof, String proofValue) {
        return Json.createObjectBuilder(document)
                .add(PROOF,
                        Json.createArrayBuilder().add(
                        Json.createObjectBuilder(proof).add(PROOF_VALUE,
                                Json.createArrayBuilder().add(Json.createObjectBuilder()
                                        .add(Keywords.VALUE, proofValue)
                                        .add(Keywords.TYPE, "https://w3id.org/security#multibase"))))
                ).build();
    }
}
