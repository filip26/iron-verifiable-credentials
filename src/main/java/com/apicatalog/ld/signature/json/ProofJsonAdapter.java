package com.apicatalog.ld.signature.json;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.proof.Proof;

import jakarta.json.JsonObject;

public interface ProofJsonAdapter {

	String type();

	Proof deserialize(JsonObject proof) throws DocumentError;

	JsonObject serialize(Proof proof) throws DocumentError;

	JsonObject setProofValue(JsonObject proof, byte[] value) throws DocumentError;

	JsonObject removeProofValue(JsonObject proof) throws DocumentError;

    VerificationMethodJsonAdapter getMethodAdapter();
}