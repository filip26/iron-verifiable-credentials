package com.apicatalog.vc;

import com.apicatalog.jsonld.lang.Keywords;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

/**
 * An embedded proof is included in the data, such as a Linked Data Signature.
 */
public class EmbeddedProof implements Proof {

    private String type;

    private String purpose;

    private String verificationMethod;
    
    private String created;
    
    private String domain;
    
    private String value;    

    /**
     * 
     * @param json expanded verifiable credentials or presentation
     * @param result
     * @return
     * @throws VerificationError
     * @throws DataIntegrityError 
     */
    static EmbeddedProof verify(final JsonObject json, final VerificationResult result) throws VerificationError, DataIntegrityError {

        final EmbeddedProof proof = from(json);

        //TODO
        return proof;
    }
    
    /**
     * 
     * @param json expanded verifiable credentials or presentation
     * @param result
     * @return
     * @throws VerificationError
     */
    static EmbeddedProof from(final JsonObject json) throws DataIntegrityError {

        if (json == null) {
            throw new IllegalArgumentException("Parameter 'json' must not be null.");
        }

        final JsonValue proofValue = json.get(Properties.PROOF);

        if (proofValue == null) {
            throw new DataIntegrityError();
        }

        if (!ValueType.ARRAY.equals(proofValue.getValueType())) {
            throw new DataIntegrityError();
        }
        
        for (JsonValue proofItem : proofValue.asJsonArray()) {
            if (!ValueType.OBJECT.equals(proofItem.getValueType())) {
                throw new DataIntegrityError();
            }

            if (proofItem.asJsonObject().containsKey(Keywords.GRAPH)) { //TODO hack
                proofItem = proofItem.asJsonObject().get(Keywords.GRAPH);
                if (ValueType.ARRAY.equals(proofItem.getValueType())) {
                    proofItem = proofItem.asJsonArray().get(0); //FIXME !?!
                }
                if (!ValueType.OBJECT.equals(proofItem.getValueType())) {
                    throw new DataIntegrityError();
                }                
            }
            
            final JsonObject proofObject = proofItem.asJsonObject();
            
            if (!proofObject.containsKey(Keywords.TYPE)
                    || !proofObject.containsKey(Properties.PROOF_PURPOSE)
                    || !proofObject.containsKey(Properties.PROOF_VERIFICATION_METHOD)
                    || !proofObject.containsKey(Properties.CREATED)
                    || !proofObject.containsKey(Properties.PROOF_VALUE)
                    ) {
                throw new DataIntegrityError();
            }

            EmbeddedProof embeddedProof = new EmbeddedProof();
            
            
            
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
    public String getVerificationMethod() {
        return verificationMethod;
    }

    @Override
    public String getCreated() {
        return created;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public String getValue() {
        return value;
    }
}
