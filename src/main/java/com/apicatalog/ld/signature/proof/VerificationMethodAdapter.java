package com.apicatalog.ld.signature.proof;

import com.apicatalog.ld.DocumentError;

import jakarta.json.JsonObject;

public interface VerificationMethodAdapter {

    String getType();

    VerificationMethod deserialize(JsonObject object) throws DocumentError;

    JsonObject serialize(VerificationMethod proof) throws DocumentError;
}
