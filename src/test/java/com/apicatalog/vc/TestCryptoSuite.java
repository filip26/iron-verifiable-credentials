package com.apicatalog.vc;

import java.net.URI;
import java.util.Arrays;

import com.apicatalog.jsonld.PropertyName;
import com.apicatalog.ld.signature.KeyGenError;
import com.apicatalog.ld.signature.LinkedDataSuiteError;
import com.apicatalog.ld.signature.SignatureSuite;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.adapter.MethodAdapter;
import com.apicatalog.ld.signature.adapter.ProofAdapter;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.ld.signature.primitive.MessageDigest;
import com.apicatalog.ld.signature.primitive.Urdna2015;
import com.apicatalog.ld.signature.proof.ProofBuilder;
import com.apicatalog.ld.signature.proof.ProofOptions;
import com.apicatalog.vc.integrity.DataIntegrityProofBuilder;

import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

class TestCryptoSuite implements SignatureSuite {

	static final URI TYPE = URI.create("https://example.org/TestSignatureSuite2022");

    @Override
    public byte[] canonicalize(JsonStructure json) throws LinkedDataSuiteError {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] digest(byte[] data) throws LinkedDataSuiteError {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void verify(byte[] publicKey, byte[] signature, byte[] data) throws VerificationError {
        // TODO Auto-generated method stub
        
    }

    @Override
    public byte[] sign(byte[] privateKey, byte[] data) throws SigningError {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public KeyPair keygen(int length) throws KeyGenError {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public URI getId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ProofAdapter getProofAdapter() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MethodAdapter getMethodAdapter(String type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DataIntegrityProofBuilder<?> createOptions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PropertyName proofValue() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PropertyName proofMethod() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JsonValue encodeProofValue(byte[] value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] decodeProofValue(JsonValue value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] decodeVerificationKey(JsonObject objectO) {
        // TODO Auto-generated method stub
        return null;
    } 
	
//	public TestCryptoSuite() {
//		super(TYPE, 
//	            new Urdna2015(),
//	            new MessageDigest("MD5"),
//	            new TestAlgorithm(),
//	            new TestProofAdapter(TYPE),
//	                Arrays.asList(
//	                    new TestVerificationMethodAdapter()
//	                    )
//				);
//
//	}
	
}
