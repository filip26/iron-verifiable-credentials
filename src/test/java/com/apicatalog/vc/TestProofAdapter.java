package com.apicatalog.vc;

import java.net.URI;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.adapter.DataIntegrityProofAdapter;

public class TestProofAdapter extends DataIntegrityProofAdapter {

	protected TestProofAdapter(URI type) {
		super(
	        type, 
	         null);	//FIXME
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

    @Override
    public URI type() {
        // TODO Auto-generated method stub
        return null;
    }
	
}

