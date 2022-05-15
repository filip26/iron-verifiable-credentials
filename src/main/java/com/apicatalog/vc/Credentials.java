package com.apicatalog.vc;

import jakarta.json.JsonObject;

public interface Credentials extends StructuredData {

    static Credentials from(JsonObject json) {
        
        final boolean verifiable = json.containsKey(Keywords.PROOF);
        
        return null;
    }

}
