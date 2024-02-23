package com.apicatalog.vc.proof;

import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.key.VerificationKey;

import jakarta.json.JsonObject;

public interface ProofValue {

    byte[] toByteArray();

    void verify(JsonObject data, VerificationKey method) throws VerificationError;
        
}
