package com.apicatalog.vc;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.json.VerificationMethodJsonAdapter;
import com.apicatalog.ld.signature.proof.VerificationMethod;

import jakarta.json.JsonObject;

public class TestVerificationMethodAdapter implements VerificationMethodJsonAdapter {

	@Override
	public String getType() {
		return "TestVerificationKey2022";
	}

	@Override
	public VerificationMethod deserialize(JsonObject object) throws DocumentError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonObject serialize(VerificationMethod proof) throws DocumentError {
		// TODO Auto-generated method stub
		return null;
	}

}
