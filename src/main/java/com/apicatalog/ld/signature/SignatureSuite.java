package com.apicatalog.ld.signature;

import com.apicatalog.ld.schema.LdSchema;
import com.apicatalog.ld.signature.proof.ProofOptions;
import com.apicatalog.ld.signature.proof.ProofType;

/**
 * A specified set of cryptographic primitives consisting of a canonicalization
 * algorithm, a message digest algorithm, and a signature algorithm.
 */
public interface SignatureSuite {

    LdSchema getSchema();
    
	ProofType getProofType();

//	ProofValueAdapter getProofValueAdapter();

//	MethodAdapter getMethodAdapter(String type);
	
	CryptoSuite getCryptoSuite();

    ProofOptions  createOptions();

    //TODO proof assertions!!!!!
    
//    /**
//     * A JSON-LD context used to expand the proof
//     * 
//     * @return an {@link URI} referencing a JSON-LD context or <code>null</code> if a context is embedded or not needed
//     */
//    public URI contexs() {
//        return context;
//    }
    
    //TODO URI getBase(URI id);
}
