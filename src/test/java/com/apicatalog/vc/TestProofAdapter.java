package com.apicatalog.vc;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.json.EmbeddedProofAdapter;

public class TestProofAdapter extends EmbeddedProofAdapter {

	protected TestProofAdapter(String type) {
		super(
	        type, 
	        new TestVerificationMethodAdapter(), null);	//FIXME
		// TODO Auto-generated constructor stub
	}

	@Override
	protected byte[] decodeValue(String encoding, String value) throws DocumentError {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String encodeValue(String encoding, byte[] value) throws DocumentError {
		// TODO Auto-generated method stub
		return null;
	}
	
}

