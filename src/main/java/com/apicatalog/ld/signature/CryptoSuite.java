package com.apicatalog.ld.signature;

import java.net.URI;

import com.apicatalog.ld.signature.adapter.MethodAdapter;
import com.apicatalog.ld.signature.adapter.ProofAdapter;
import com.apicatalog.ld.signature.algorithm.CanonicalizationAlgorithm;
import com.apicatalog.ld.signature.algorithm.DigestAlgorithm;
import com.apicatalog.ld.signature.algorithm.SignatureAlgorithm;
import com.apicatalog.ld.signature.key.KeyPair;

import jakarta.json.JsonStructure;

/**
 * A specified set of cryptographic primitives consisting of a canonicalization
 * algorithm, a message digest algorithm, and a signature algorithm.
 */
public interface CryptoSuite extends CanonicalizationAlgorithm, DigestAlgorithm, SignatureAlgorithm {

	void verify(byte[] publicKey, byte[] signature, byte[] data) throws VerificationError;

	byte[] sign(byte[] privateKey, byte[] data) throws SigningError;


	byte[] digest(byte[] data) throws LinkedDataSuiteError;

	byte[] canonicalize(JsonStructure document) throws LinkedDataSuiteError;

	KeyPair keygen(int length) throws KeyGenError;

	URI id();

	ProofAdapter getProofAdapter();

    MethodAdapter getMethodAdapter(String type);
    
    /**
     * A JSON-LD context used to expand the proof
     * 
     * @return an {@link URI} referencing a JSON-LD context or <code>null</code> if a context is embedded or not needed
     */
    URI contexs();
    
    //TODO URI getBase(URI id);
}
