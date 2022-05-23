package com.apicatalog.vc.proof;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Set;
import java.util.stream.Collectors;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.lang.NodeObject;
import com.apicatalog.jsonld.lang.ValueObject;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.vc.Constants;
import com.apicatalog.vc.DataIntegrityError;
import com.apicatalog.vc.VerificationError;
import com.apicatalog.vc.VerificationError.Type;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

/**
 * An embedded proof is included in the data, such as a Linked Data Signature.
 */
public class EmbeddedProof implements Proof {

    private Set<String> type;

    private String purpose;

    private String verificationMethod;
    
    private Instant created;
    
    private String domain;
    
    private ProofValue value;    

    /**
     * 
     * @param json expanded verifiable credentials or presentation
     * @param result
     * @return
     * @throws VerificationError
     */
    public static EmbeddedProof from(final JsonObject json) throws DataIntegrityError {

        if (json == null) {
            throw new IllegalArgumentException("Parameter 'json' must not be null.");
        }

        final JsonValue proofValue = json.get(Constants.PROOF);

        if (proofValue == null) {
            throw new DataIntegrityError();
        }

        if (!ValueType.ARRAY.equals(proofValue.getValueType())) {
            throw new DataIntegrityError();
        }
        
        for (JsonValue proofItem : proofValue.asJsonArray()) {
            
            // data integrity checks
            if (JsonUtils.isNotObject(proofItem)) {
                throw new DataIntegrityError();
            }

            if (proofItem.asJsonObject().containsKey(Keywords.GRAPH)) { //TODO hack
                proofItem = proofItem.asJsonObject().get(Keywords.GRAPH);
                if (JsonUtils.isArray(proofItem)) {
                    proofItem = proofItem.asJsonArray().get(0); //FIXME !?!
                }
                if (JsonUtils.isNotObject(proofItem)) {
                    throw new DataIntegrityError();
                }                
            }

            final JsonObject proofObject = proofItem.asJsonObject();
            
            final EmbeddedProof embeddedProof = new EmbeddedProof();

            // @type property
            if (!proofObject.containsKey(Keywords.TYPE)) {
                throw new DataIntegrityError();
            }

            final JsonValue typeValue = proofObject.get(Keywords.TYPE);
            
            if (JsonUtils.isArray(typeValue)) {
                
                // all @type values must be string
                if (!typeValue.asJsonArray().stream().allMatch(JsonUtils::isString)) {
                    throw new DataIntegrityError();
                }
                
                embeddedProof.type = typeValue.asJsonArray().stream()
                                        .map(JsonString.class::cast)
                                        .map(JsonString::getString)
                                        .collect(Collectors.toSet());
                
            } else if (JsonUtils.isString(typeValue)) {
                embeddedProof.type = Set.of(((JsonString)typeValue).getString());
                
            } else {
                throw new DataIntegrityError();
            }

            // proofPurpose property
            if (!proofObject.containsKey(Constants.PROOF_PURPOSE)) {
                throw new DataIntegrityError();
            }

            final JsonValue proofPurposeValue = proofObject.get(Constants.PROOF_PURPOSE);
            
            if (JsonUtils.isArray(proofPurposeValue)) {
                 
                if (!proofPurposeValue.asJsonArray().stream().allMatch(NodeObject::isNodeReference)) {
                    throw new DataIntegrityError();
                }
                
                embeddedProof.purpose = proofPurposeValue.asJsonArray().stream()
                                        .map(JsonValue::asJsonObject)
                                        .map(o -> o.getString(Keywords.ID))
                                        .limit(1).toArray(String[]::new)[0];
            } else {
                throw new DataIntegrityError();
            }

            // verificationMethod property
            if (!proofObject.containsKey(Constants.PROOF_VERIFICATION_METHOD)) {
                throw new DataIntegrityError();
            }

            final JsonValue verificationMethodValue = proofObject.get(Constants.PROOF_VERIFICATION_METHOD);
            
            if (JsonUtils.isArray(verificationMethodValue)) {
                 
                if (!verificationMethodValue.asJsonArray().stream().allMatch(NodeObject::isNodeReference)) {
                    throw new DataIntegrityError();
                }
                
                embeddedProof.verificationMethod = verificationMethodValue.asJsonArray().stream()
                                        .map(JsonValue::asJsonObject)
                                        .map(o -> o.getString(Keywords.ID))
                                        .limit(1).toArray(String[]::new)[0];
            } else {
                throw new DataIntegrityError();
            }

            // proofValue property
            if (!proofObject.containsKey(Constants.PROOF_VALUE)) {
                throw new DataIntegrityError();
            }

            final JsonValue embeddedProofValue = proofObject.get(Constants.PROOF_VALUE);
            
            if (JsonUtils.isArray(embeddedProofValue)) {
                 
                if (!embeddedProofValue.asJsonArray().stream().allMatch(ValueObject::isValueObject)) {
                    throw new DataIntegrityError();
                }

                embeddedProof.value = embeddedProofValue.asJsonArray().stream()
                                        .map(JsonValue::asJsonObject)
                                        .map(o -> new ProofValue(o.getString(Keywords.VALUE),
                                                JsonUtils.toStream(o.get(Keywords.TYPE))
                                                .map(JsonString.class::cast)
                                                .map(JsonString::getString)
                                                .collect(Collectors.toSet())
                                                
                                                   ))
                                        .limit(1).toArray(ProofValue[]::new)[0];
            } else {
                throw new DataIntegrityError();
            }
            
            // created property
            if (!proofObject.containsKey(Constants.CREATED)) {
                throw new DataIntegrityError();
            }

            final JsonValue createdValue = proofObject.get(Constants.CREATED);
            
            if (JsonUtils.isArray(createdValue)) {

                // take first created property
                final JsonValue createdItem = createdValue.asJsonArray().get(0);
                
                // expect value object and date in ISO 8601 format
                if (!ValueObject.isValueObject(createdItem)) {
                    throw new DataIntegrityError();
                }

                //TODO check @type
                
                String createdString = ValueObject.getValue(createdItem).filter(JsonUtils::isString)
                .map(JsonString.class::cast)
                .map(JsonString::getString).orElseThrow(DataIntegrityError::new);

                try {
                    OffsetDateTime created = OffsetDateTime.parse(createdString);
                    
                    embeddedProof.created = created.toInstant(); 
                
                } catch (DateTimeParseException e) {
                    throw new DataIntegrityError();
                }
                

            } else {
                throw new DataIntegrityError();
            }

            
            //TODO domain property
            


            return embeddedProof;       //FIXME process other proofs
        }


        //TODO
        return null;
    }    
    

    @Override
    public Set<String> getType() {
        return type;
    }


    @Override
    public String getPurpose() {
        return purpose;
    }

    @Override
    public String getVerificationMethod() {
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
    public ProofValue getValue() {
        return value;
    }

    @Override
    public void verify() throws VerificationError {

        // verify supported crypto suite
        if (!isTypeOf("https://w3id.org/security#Ed25519Signature2020")) {
            throw new VerificationError(Type.UnknownCryptoSuiteType);
        }

        // verify supported proof value encoding
        if (value == null && !value.isTypeOf("https://w3id.org/security#multibase")) {
            throw new VerificationError(Type.InvalidProofValue);
        }

        // verify proof value
        if (value.getValue() == null || !Multibase.isAlgorithmSupported(value.getValue())) {
            throw new VerificationError(Type.InvalidProofValue);
        }
      
        // decode proof value
        byte[] proofValue = Multibase.decode(value.getValue());
      
        // verify proof value length
        if (proofValue.length != 64) {
            throw new VerificationError(Type.InvalidProofLenght);
        }

        // TODO Auto-generated method stub        
    }
}
