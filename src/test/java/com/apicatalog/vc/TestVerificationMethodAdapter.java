package com.apicatalog.vc;

import java.net.URI;

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
    public URI type() {
        // TODO Auto-generated method stub
        return null;
    }

}
