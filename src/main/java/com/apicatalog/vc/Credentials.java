package com.apicatalog.vc;

import jakarta.json.JsonObject;

public interface Credentials extends StructuredData {

    static Credentials from(JsonObject json) {
        
        if (json == null) {
            throw new IllegalArgumentException("Parameter 'json' must not be null.");
        }

        final boolean verifiable = json.containsKey(Keywords.PROOF);
        
        final Credentials credentials = new ImmutableVerifiableCredentials(null, null); 
                
        return credentials;
    }

}
