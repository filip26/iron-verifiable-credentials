package com.apicatalog.vc.proof;

import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.VerificationError;

import jakarta.json.JsonObject;

public interface ProofValue {

    void verify(CryptoSuite cryptoSuite, JsonObject data, JsonObject unsignedProof, byte[] publicKey) throws VerificationError;
}
