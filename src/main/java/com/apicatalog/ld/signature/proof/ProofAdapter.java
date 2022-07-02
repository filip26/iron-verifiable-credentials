package com.apicatalog.ld.signature.proof;

import com.apicatalog.ld.DocumentError;

import jakarta.json.JsonObject;

public interface ProofAdapter {

    String type();

    Proof deserialize(JsonObject object) throws DocumentError;

    JsonObject serialize(Proof proof) throws DocumentError;

    JsonObject setProofValue(JsonObject proof, byte[] value) throws DocumentError;

    VerificationMethodAdapter getMethodAdapter();
}