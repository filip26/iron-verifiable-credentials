package com.apicatalog.vc;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

public class EmbeddedProof implements Proof {

    static void verify(final JsonObject json) throws VerificationError {
        
        if (json == null) {
            throw new IllegalArgumentException("Parameter 'json' must not be null.");
        }

        final JsonValue proofValue = json.get(Keywords.PROOF);
        
        if (proofValue == null) {
            throw new VerificationError();
        }
        
        if (!ValueType.OBJECT.equals(proofValue.getValueType())) {
            throw new VerificationError();
        }

        final JsonObject proof = proofValue.asJsonObject();
        
        
        
        
    }
    
}
