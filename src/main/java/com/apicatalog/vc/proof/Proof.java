package com.apicatalog.vc.proof;

import java.time.Instant;
import java.util.Set;

import com.apicatalog.vc.VerificationError;

import jakarta.json.JsonArray;


public interface Proof {

    /**
     * The proof type used.
     *  
     * For example, an Ed25519Signature2020 type indicates that the proof includes 
     * a digital signature produced by an ed25519 cryptographic key.
     * 
     * @return
     */
    Set<String> getType();
    
    /**
     * The intent for the proof, the reason why an entity created it
     * 
     * @return
     */
    String getPurpose();

    /**
     * A set of parameters required to independently verify the proof,
     * such as an identifier for a public/private key pair that would be used in the proof.
     * 
     * @return
     */
    VerificationMethod getVerificationMethod();
    
    /**
     * The string value of an ISO8601.
     * 
     * @return
     */
    Instant getCreated();
    
    /**
     * A string value specifying the restricted domain of the proof.
     * @return
     */
    String getDomain();

    /**
     * One of any number of valid representations of proof value generated by the Proof Algorithm.
     * @return
     */
    ProofValue getValue();

    /**
     * Checks is the proof is of the given type.
     * 
     * @param type
     * @return <code>true</code> if the given type matches the proof type
     */
    default boolean isTypeOf(final String type) {
        return getType() != null && getType().contains(type);
    }

    void verify(JsonArray document) throws VerificationError;    
}