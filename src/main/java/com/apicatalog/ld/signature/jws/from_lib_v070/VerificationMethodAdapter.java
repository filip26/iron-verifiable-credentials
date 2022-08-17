package com.apicatalog.ld.signature.jws.from_lib_v070;

import com.apicatalog.ld.DocumentError;
import jakarta.json.JsonObject;
import com.apicatalog.ld.signature.proof.VerificationMethod;

/**
 * Originally interface com.apicatalog.ld.signature.proof.VerificationMethodAdapter (in library version 0.7.0)
 */
public interface VerificationMethodAdapter {

    String getType();

    VerificationMethod deserialize(JsonObject object) throws DocumentError;

    JsonObject serialize(VerificationMethod proof) throws DocumentError;
}
