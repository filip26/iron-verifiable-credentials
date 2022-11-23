package com.apicatalog.vc;

import com.apicatalog.ld.signature.SignatureSuite;
import com.apicatalog.ld.signature.primitive.MessageDigest;
import com.apicatalog.ld.signature.primitive.Urdna2015;

class TestCryptoSuite extends SignatureSuite {

	static final String TYPE = "https://example.org/TestSignatureSuite2022"; 
	
	public TestCryptoSuite() {
		super(TYPE, 
	            new Urdna2015(),
	            new MessageDigest("MD5"),
	            new TestAlgorithm(),
	            new TestProofAdapter(TYPE)	,
	            new TestVerificationMethodAdapter()
				);

	}
}
