package com.apicatalog.vc;

import java.net.URI;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.json.VerificationMethodJsonAdapter;
import com.apicatalog.ld.signature.proof.VerificationMethod;

import jakarta.json.JsonObject;

public class TestVerificationMethodAdapter implements VerificationMethodJsonAdapter {

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
    public boolean isSupportedType(String type) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public URI getContextFor(URI id) {
        // TODO Auto-generated method stub
        return null;
    }

}
