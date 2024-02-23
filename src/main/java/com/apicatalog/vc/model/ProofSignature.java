package com.apicatalog.vc.model;

import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.key.VerificationKey;

import jakarta.json.JsonObject;

public interface ProofSignature {

    byte[] toByteArray();

    void verify(JsonObject data, VerificationKey method) throws VerificationError;
        
}
