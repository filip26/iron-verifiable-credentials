package com.apicatalog.vc.proof;

import java.net.URI;
import java.util.Map;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.ld.signature.key.VerificationKey;
import com.apicatalog.vc.integrity.DataIntegrityProof;
import com.apicatalog.vc.method.MethodAdapter;

import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;

/**
 * Represents generic VC/VP proof.
 *
 * @see {@link DataIntegrityProof} for an example implementation
 */
public interface Proof {

    /**
     * A set of parameters required to independently verify the proof, such as an
     * identifier for a public/private key pair that would be used in the proof.
     * Mandatory
     *
     * @return {@link VerificationMethod} to verify the proof signature
     */
    VerificationMethod method();

    /**
     * One of any number of valid representations of proof value generated by the
     * Proof Algorithm.
     *
     * @return the proof value
     */
    ProofValue signature();

    /**
     * The proof unique identifier. Optional.
     * 
     * @return {@link URI} representing the proof id
     */
    URI id();

    /**
     * Must be processed after the previous proof. Allow to create a chain of
     * proofs. Optional.
     * 
     * @return {@link URI} uniquely identifying the previous proof
     */
    URI previousProof();
    
    /**
     * Returns a {@link CryptoSuite} used to create and to verify the proof value.
     * 
     * @return {@link CryptoSuite} attached to the proof.
     */
    CryptoSuite cryptoSuite();
    
    /**
     * Validates the proof data, not a signature.
     * 
     * @param params custom, suite specific parameters to validate against
     * 
     * @throws DocumentError if the proof is not valid, e.g. is created in the
     *                       future
     */
    void validate(Map<String, Object> params) throws DocumentError;

    void verify(JsonStructure context, JsonObject data, VerificationKey method) throws VerificationError;

    MethodAdapter methodProcessor();
}