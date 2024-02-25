package com.apicatalog.vc.proof;

import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.VerificationError;

import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;

public interface ProofValue {

    void verify(CryptoSuite crypto, JsonStructure context, JsonObject data, JsonObject unsignedProof, byte[] publicKey) throws VerificationError;

}
