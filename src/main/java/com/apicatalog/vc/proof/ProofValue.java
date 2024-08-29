package com.apicatalog.vc.proof;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.linkedtree.Linkable;

import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;

public interface ProofValue extends Linkable {

    void verify(CryptoSuite crypto, JsonStructure context, JsonObject data, JsonObject unsignedProof, byte[] publicKey) throws VerificationError, DocumentError;

    byte[] toByteArray() throws DocumentError;
}
