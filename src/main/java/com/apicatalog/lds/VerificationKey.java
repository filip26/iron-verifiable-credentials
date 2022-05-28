package com.apicatalog.lds;

import jakarta.json.JsonObject;

public interface VerificationKey {

    String getId();
    String getType();
    
    byte[] getPublicKey();
    
    JsonObject toJson();
}
