package com.apicatalog.vc;

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

        final JsonValue proofValue = json.get(Keywords.PROOF);

        if (proofValue == null) {
            throw new DataIntegrityError();
        }

        if (!ValueType.ARRAY.equals(proofValue.getValueType())) {
            throw new DataIntegrityError();
        }
        
        for (final JsonValue proofItem : proofValue.asJsonArray()) {
            if (!ValueType.OBJECT.equals(proofItem.getValueType())) {
                throw new DataIntegrityError();
            }
            
            final JsonObject proofObject = proofItem.asJsonObject();
            
            if (!proofObject.containsKey(Keywords.TYPE)
                    || !proofObject.containsKey(Keywords.PROOF_PURPOSE)
                    || !proofObject.containsKey(Keywords.PROOF_VERIFICATION_METHOD)
                    || !proofObject.containsKey(Keywords.PROOF_CREATED)
                    || !proofObject.containsKey(Keywords.PROOF_VALUE)
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
