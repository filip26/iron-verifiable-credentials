package com.apicatalog.vc.proof;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.node.LdScalar;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.VerificationError;

import jakarta.json.JsonObject;

public interface ProofValue {

    void validate() throws DocumentError;

    void set(byte[] signature);

    void set(LdScalar scalar) throws DocumentError;

    JsonObject expand();

    void verify(CryptoSuite cryptoSuite, JsonObject data, JsonObject unsignedProof, byte[] publicKey) throws VerificationError;

    int length();
}
