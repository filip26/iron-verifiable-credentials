package com.apicatalog.ld.signature.json;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.proof.VerificationMethod;

import jakarta.json.JsonObject;

public interface VerificationMethodJsonAdapter {

    String getType();

    VerificationMethod deserialize(JsonObject object) throws DocumentError;

    JsonObject serialize(VerificationMethod proof) throws DocumentError;
}
