package com.apicatalog.vc;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

/**
 * An embedded proof is included in the data, such as a Linked Data Signature.
 */
public class EmbeddedProof implements Proof {

    /**
     * 
     * @param json expanded verifiable credentials or presentation
     * @param result
     * @return
     * @throws VerificationError
     */
    static EmbeddedProof verify(final JsonObject json, final VerificationResult result) throws VerificationError {

        if (json == null) {
            throw new IllegalArgumentException("Parameter 'json' must not be null.");
        }

        final JsonValue proofValue = json.get(Keywords.PROOF);

        if (proofValue == null) {
            throw new VerificationError();
        }

        if (!ValueType.ARRAY.equals(proofValue.getValueType())) {
            throw new VerificationError();
        }
        
        for (final JsonValue proofItem : proofValue.asJsonArray()) {
            if (!ValueType.OBJECT.equals(proofItem.getValueType())) {
                throw new VerificationError();
            }
            
            //TODO parse embedded proof
            
        }


        //TODO
        return null;
    }

}
