package com.apicatalog.ld.signature.proof;

import com.apicatalog.ld.signature.DataError;

import jakarta.json.JsonObject;

public interface VerificationMethodAdapter {

    String getType();
    
    VerificationMethod deserialize(JsonObject object) throws DataError;

    JsonObject serialize(VerificationMethod proof) throws DataError;
}
