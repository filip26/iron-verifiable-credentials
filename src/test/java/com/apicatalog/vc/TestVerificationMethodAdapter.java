package com.apicatalog.vc;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.json.MethodAdapter;
import com.apicatalog.ld.signature.method.VerificationMethod;

import jakarta.json.JsonObject;

public class TestVerificationMethodAdapter implements MethodAdapter {

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

    @Override
    public String type() {
        // TODO Auto-generated method stub
        return null;
    }

}
