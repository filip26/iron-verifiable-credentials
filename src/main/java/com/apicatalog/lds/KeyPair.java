package com.apicatalog.lds;

import jakarta.json.JsonObject;

public interface KeyPair {

    String getType();
    
    byte[] getPublicKey();
    byte[] getPrivateKey();
    
    JsonObject toJson();
}
