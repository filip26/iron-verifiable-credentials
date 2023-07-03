package com.apicatalog.vc.model;

import java.net.URI;
import java.util.Map;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.vc.integrity.DataIntegrityProof;
import com.apicatalog.vc.suite.SignatureSuite;

import jakarta.json.JsonObject;

/**
 * Represents generic VC/VP proof.
 *
 * @see {@link DataIntegrityProof} for an example implementation
 */
public interface Proof {

    /**
     * The proof type used.
     *
     * For example, an Ed25519Signature2020 type indicates that the proof includes a
     * digital signature produced by an ed25519 cryptographic key. DataIntegrityProof 
     * type indicates generic data integrity proof type.
     *
     * @return the proof type
     */
    String getType();

    /**
     * A set of parameters required to independently verify the proof, such as an
     * identifier for a public/private key pair that would be used in the proof.
     * Mandatory
     *
     * @return {@link VerificationMethod} to verify the proof signature
     */
    VerificationMethod getMethod();

    /**
     * One of any number of valid representations of proof value generated by the
     * Proof Algorithm.
     *
     * @return the proof value as byte array
     */
    byte[] getValue();
    
    /**
     * The proof unique identifier. Optional.
     * 
     * @return {@link URI} representing the proof id
     */
    URI id();
    
    /**
     * Must be processed after the previous proof. Allow to
     * create a chain of proofs. Optional.
     * 
     * @return {@link URI} uniquely identifying the previous proof
     */
    URI previousProof();

    /**
     * Returns a {@link CryptoSuite} used to create and to verify
     * the proof value.
     * 
     * @return {@link CryptoSuite} attached to the proof.
     */
    CryptoSuite getCryptoSuite();
    
    /**
     * Returns a signature suite this proof implementation belongs to.
     * 
     * @return {@link SignatureSuite} implementing the proof type.
     */
    SignatureSuite getSignatureSuite();

    /**
     * Validates the proof data, not a signature. 
     *  
     * @param params custom, suite specific parameters to validate against
     * 
     * @throws DocumentError if the proof is not valid, e.g. is created in the future
     */
    void validate(Map<String, Object> params) throws DocumentError;

    /**
     * Returns JSON-LD expanded form of the proof. 
     *  
     * @return the proof in an expanded JSON-LD form
     */
    JsonObject toJsonLd();
    
    /**
     * Provides an external JSON-LD context URI defying the proof type. 
     * The context URI is used to expand the deferred proof verification method.
     *  
     * @return JSON-LD context URI or <code>null</code> (default)
     */
    default String context() {
        //FIXME VerificationMethodProcessor ???
            // context();
        return null;
    }
    
    ProofValueProcessor valueProcessor();
}
