package com.apicatalog.vc;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.KeyGenError;
import com.apicatalog.ld.signature.SignatureSuite;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.algorithm.SignatureAlgorithm;
import com.apicatalog.ld.signature.json.EmbeddedProofAdapter;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.ld.signature.primitive.MessageDigest;
import com.apicatalog.ld.signature.primitive.Urdna2015;

class TestCryptoSuite extends SignatureSuite {

	public TestCryptoSuite() {
		super("TestCryptoSuite", 
	            new Urdna2015(),
	            new MessageDigest("MD5"),
	            new TestAlgorithm(),
	            new TestProofAdapter()	
				);

	}
}
