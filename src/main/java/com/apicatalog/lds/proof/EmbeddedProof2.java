package com.apicatalog.lds.proof;

import java.time.Instant;

/**
 * An embedded proof is included in the data, such as a Linked Data Signature.
 */
public class EmbeddedProof2 implements Proof {

    private String type;

    private String purpose;

    private VerificationMethod verificationMethod;
    
    private Instant created;
    
    private String domain;
    
    private byte[] value;    

    public static EmbeddedProof2 from(ProofOptions options) {
        final EmbeddedProof2 proof = new EmbeddedProof2();

        proof.type = options.getType();
        proof.verificationMethod = options.getVerificationMethod();
        proof.created = options.getCreated();
        proof.domain = options.getDomain();
        
        return proof;
    }

    @Override
    public String getType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPurpose() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public VerificationMethod getVerificationMethod() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Instant getCreated() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDomain() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] getValue() {
        // TODO Auto-generated method stub
        return null;
    }
    
//    /**
//     * 
//     * @param json expanded verifiable credentials or presentation
//     * @param result
//     * @return
//     * @throws VerificationError
//     */
//    public static EmbeddedProof2 from(final JsonObject json, final DocumentLoader loader) throws DataIntegrityError {
//
//        if (json == null) {
//            throw new IllegalArgumentException("Parameter 'json' must not be null.");
//        }
//
//        final JsonValue proofValue = json.get(Constants.PROOF);
//
//
//        if (proofValue == null) {
//            throw new DataIntegrityError();
//        }
//
//        if (!ValueType.ARRAY.equals(proofValue.getValueType())) {
//            throw new DataIntegrityError();
//        }
//        
//        for (JsonValue proofItem : proofValue.asJsonArray()) {
//            
//            // data integrity checks
//            if (JsonUtils.isNotObject(proofItem)) {
//                throw new DataIntegrityError();
//            }
//
//            if (proofItem.asJsonObject().containsKey(Keywords.GRAPH)) { //TODO hack
//                proofItem = proofItem.asJsonObject().get(Keywords.GRAPH);
//                if (JsonUtils.isArray(proofItem)) {
//                    proofItem = proofItem.asJsonArray().get(0); //FIXME !?!
//                }
//                if (JsonUtils.isNotObject(proofItem)) {
//                    throw new DataIntegrityError();
//                }                
//            }
//
//            final JsonObject proofObject = proofItem.asJsonObject();
//            
//            final EmbeddedProof2 embeddedProof = new EmbeddedProof2();
//
//            // @type property
//            if (!proofObject.containsKey(Keywords.TYPE)) {
//                throw new DataIntegrityError();
//            }
//
//            final JsonValue typeValue = proofObject.get(Keywords.TYPE);
//            
//            if (JsonUtils.isArray(typeValue)) {
//                
//                // all @type values must be string
//                if (!typeValue.asJsonArray().stream().allMatch(JsonUtils::isString)) {
//                    throw new DataIntegrityError();
//                }
//                
//                embeddedProof.type = typeValue.asJsonArray().stream()
//                                        .map(JsonString.class::cast)
//                                        .map(JsonString::getString)
//                                        .findAny().orElse(null);
//                
//            } else if (JsonUtils.isString(typeValue)) {
//                embeddedProof.type = ((JsonString)typeValue).getString();
//                
//            } else {
//                throw new DataIntegrityError();
//            }
//
//            // proofPurpose property
//            if (!proofObject.containsKey(Constants.PROOF_PURPOSE)) {
//                throw new DataIntegrityError();
//            }
//
//            final JsonValue proofPurposeValue = proofObject.get(Constants.PROOF_PURPOSE);
//            
//            if (JsonUtils.isArray(proofPurposeValue)) {
//                 
//                if (!proofPurposeValue.asJsonArray().stream().allMatch(NodeObject::isNodeReference)) {
//                    throw new DataIntegrityError();
//                }
//                
//                embeddedProof.purpose = proofPurposeValue.asJsonArray().stream()
//                                        .map(JsonValue::asJsonObject)
//                                        .map(o -> o.getString(Keywords.ID))
//                                        .limit(1).toArray(String[]::new)[0];
//            } else {
//                throw new DataIntegrityError();
//            }
//
//            // verificationMethod property
//            if (!proofObject.containsKey(Constants.PROOF_VERIFICATION_METHOD)) {
//                throw new DataIntegrityError();
//            }
//
//            final JsonValue verificationMethodValue = proofObject.get(Constants.PROOF_VERIFICATION_METHOD);
//            
//            if (JsonUtils.isArray(verificationMethodValue) && verificationMethodValue.asJsonArray().size() > 0) {
//
//                final JsonValue verificationMethodItem = verificationMethodValue.asJsonArray().get(0);
//                                
//                if (NodeObject.isNodeReference(verificationMethodItem)) { 
//
//                    final JsonObject verificationMethodObject = verificationMethodItem.asJsonObject();
//
//                    final String id = verificationMethodObject.getString(Keywords.ID);
//                    //TODO check verification method type
//                    
//                    embeddedProof.verificationMethod = Ed25519KeyPair2020.reference(URI.create(id));
//                    
//                //TODO embedded key
//                    
//                } else {
//                    throw new DataIntegrityError();
//                }
//                
//                
//            } else {
//                throw new DataIntegrityError();
//            }
//
//            // proofValue property
//            if (!proofObject.containsKey(Constants.PROOF_VALUE)) {
//                throw new DataIntegrityError();
//            }
//
//            final JsonValue embeddedProofValue = proofObject.get(Constants.PROOF_VALUE);
//            
//            if (JsonUtils.isArray(embeddedProofValue)) {
//                 
//                if (!embeddedProofValue.asJsonArray().stream().allMatch(ValueObject::isValueObject)
//                        || !embeddedProofValue.asJsonArray().stream()
//                                .map(JsonValue::asJsonObject)
//                                .map(o -> o.get(Keywords.VALUE))
//                                .allMatch(JsonUtils::isString)
//                        ) {
//                    throw new DataIntegrityError();
//                }
//                
//
//                String proofValueType = embeddedProofValue.asJsonArray().getJsonObject(0).getString(Keywords.TYPE);
//                                
//                String encodedProofValue =  embeddedProofValue.asJsonArray().getJsonObject(0).getString(Keywords.VALUE);
//                
//                // verify supported proof value encoding
//                if (!"https://w3id.org/security#multibase".equals(proofValueType)) {
//                    throw new DataIntegrityError(Code.InvalidProofValue);        //FIXME belongs to ED25...
//                }
//
//                // verify proof value
//                if (encodedProofValue == null || !Multibase.isAlgorithmSupported(encodedProofValue)) {
//                    throw new DataIntegrityError(Code.InvalidProofValue);
//                }
//
//                // decode proof value
//                byte[] rawProofValue = Multibase.decode(encodedProofValue);
//              
//                // verify proof value length
//                if (rawProofValue.length != 64) {
//                    throw new DataIntegrityError(Code.InvalidProofLength);
//                }
//                
//                embeddedProof.value = rawProofValue;
//                
//            } else {
//                throw new DataIntegrityError();
//            }
//            
//            // created property
//            if (!proofObject.containsKey(Constants.CREATED)) {
//                throw new DataIntegrityError();
//            }
//
//            final JsonValue createdValue = proofObject.get(Constants.CREATED);
//            
//            if (JsonUtils.isArray(createdValue)) {
//
//                // take first created property
//                final JsonValue createdItem = createdValue.asJsonArray().get(0);
//                
//                // expect value object and date in ISO 8601 format
//                if (!ValueObject.isValueObject(createdItem)) {
//                    throw new DataIntegrityError();
//                }
//
//                //TODO check @type
//                
//                String createdString = ValueObject.getValue(createdItem).filter(JsonUtils::isString)
//                .map(JsonString.class::cast)
//                .map(JsonString::getString).orElseThrow(DataIntegrityError::new);
//
//                try {
//                    OffsetDateTime created = OffsetDateTime.parse(createdString);
//                    
//                    embeddedProof.created = created.toInstant(); 
//                
//                } catch (DateTimeParseException e) {
//                    throw new DataIntegrityError();
//                }
//                
//
//            } else {
//                throw new DataIntegrityError();
//            }
//
//            
//            //TODO domain property
//            
//
//
//            return embeddedProof;       //FIXME process other proofs
//        }
//        
//
//
//
//        //TODO
//        return null;
//    }    
//    
//    @Override
//    public String getType() {
//        return type;
//    }
//
//
//    @Override
//    public String getPurpose() {
//        return purpose;
//    }
//
//    @Override
//    public VerificationMethod getVerificationMethod() {
//        return verificationMethod;
//    }
//
//    @Override
//    public Instant getCreated() {
//        return created;
//    }
//
//    @Override
//    public String getDomain() {
//        return domain;
//    }
//
//    @Override
//    public byte[] getValue() {
//        return value;
//    }
//    
//    @Override
//    public JsonObject toJson() {
//        return Json.createObjectBuilder().add(Keywords.TYPE, Json.createArrayBuilder().add(type))
//
//                .add(Constants.PROOF_VERIFICATION_METHOD,
//                        Json.createArrayBuilder()
//                                .add(Json.createObjectBuilder().add(Keywords.ID,
//                                        verificationMethod.getId().toString())))
//                .add(Constants.CREATED,
//                        Json.createArrayBuilder()
//                                .add(Json.createObjectBuilder()
//                                        .add(Keywords.TYPE, "http://www.w3.org/2001/XMLSchema#dateTime")
//                                        .add(Keywords.VALUE, created.toString())
//
//                                ))
//                .add(Constants.PROOF_PURPOSE,
//                        Json.createArrayBuilder()
//                                .add(Json.createObjectBuilder().add(Keywords.ID,
//                                        "https://w3id.org/security#assertionMethod")))  //FIXME configurable
//                // TODO domain to proof
//
//                .build();
//    }
//
//    public static JsonObject setProof(JsonObject document, JsonObject proof, String proofValue) {
//        return Json.createObjectBuilder(document)
//                .add(Constants.PROOF,
//                        Json.createArrayBuilder().add(
//                        Json.createObjectBuilder(proof).add(Constants.PROOF_VALUE,
//                                Json.createArrayBuilder().add(Json.createObjectBuilder()
//                                        .add(Keywords.VALUE, proofValue)
//                                        .add(Keywords.TYPE, "https://w3id.org/security#multibase"))))
//                ).build();
//    }
}