package com.apicatalog.ld.signature.proof;

import com.apicatalog.ld.signature.DataError;
import com.apicatalog.ld.signature.key.VerificationMethodAdapter;

import jakarta.json.JsonObject;

public interface ProofAdapter {

    String type();

    Proof deserialize(JsonObject object) throws DataError;

    JsonObject serialize(Proof proof) throws DataError;

    JsonObject setProofValue(JsonObject proof, byte[] value);
    
    VerificationMethodAdapter getKeyAdapter();
}