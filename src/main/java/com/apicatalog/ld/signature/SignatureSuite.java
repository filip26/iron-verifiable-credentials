package com.apicatalog.ld.signature;

import java.net.URI;

import com.apicatalog.jsonld.PropertyName;
import com.apicatalog.ld.signature.adapter.MethodAdapter;
import com.apicatalog.ld.signature.adapter.ProofAdapter;
import com.apicatalog.ld.signature.algorithm.CanonicalizationAlgorithm;
import com.apicatalog.ld.signature.algorithm.DigestAlgorithm;
import com.apicatalog.ld.signature.algorithm.SignatureAlgorithm;
import com.apicatalog.ld.signature.method.VerificationMethod;
import com.apicatalog.ld.signature.proof.Proof;
import com.apicatalog.ld.signature.proof.ProofBuilder;
import com.apicatalog.ld.signature.proof.ProofOptions;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

/**
 * A specified set of cryptographic primitives consisting of a canonicalization
 * algorithm, a message digest algorithm, and a signature algorithm.
 */
public interface SignatureSuite<P extends Proof> extends CanonicalizationAlgorithm, DigestAlgorithm, SignatureAlgorithm {

	URI getId();

	ProofAdapter<P> getProofAdapter();

	MethodAdapter getMethodAdapter(String type);

    <O extends ProofOptions> ProofBuilder<O>  createOptions();

    PropertyName proofValue();

    PropertyName proofMethod();

    JsonValue encodeProofValue(byte[] value);
    
    byte[] decodeProofValue(JsonValue value);
    
    byte[] decodeVerificationKey(JsonObject objectO);
    
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
