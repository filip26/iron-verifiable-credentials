package com.apicatalog.vc;

import com.apicatalog.ld.signature.KeyGenError;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.algorithm.SignatureAlgorithm;
import com.apicatalog.ld.signature.key.KeyPair;

class TestAlgorithm implements SignatureAlgorithm {

	@Override
	public void verify(byte[] publicKey, byte[] signature, byte[] data) throws VerificationError {
		
		
	}

	@Override
	public byte[] sign(byte[] privateKey, byte[] data) throws SigningError {
		System.out.println("SIGN " + data.length);
		return data;
	}

	@Override
	public KeyPair keygen(int length) throws KeyGenError {
		throw new UnsupportedOperationException();
	}
}
