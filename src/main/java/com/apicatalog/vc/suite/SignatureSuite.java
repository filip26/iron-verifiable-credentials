package com.apicatalog.vc.suite;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.vc.issuer.Issuer;
import com.apicatalog.vc.proof.Proof;

import jakarta.json.JsonObject;

public interface SignatureSuite {

    /**
     * Check if the given proof can be processed by the suite.
     * 
     * @param proofType an URI representing a proof JSON-LD type
     * @param proof     a proof in an expanded JSON-LD form
     * 
     * @return <code>true</code> if the proof is supported, <code>false</code>
     *         otherwise
     */
    boolean isSupported(String proofType, JsonObject proof);

    /**
     * Deserialize the given expanded JSON-LD object into a {@link Proof}.
     * 
     * @param proof JSON-LD object in an expanded form
     * 
     * @return a new {@link Proof} instance
     * @throws DocumentError if the given object cannot be deserialized
     */
    Proof getProof(JsonObject proof) throws DocumentError;

    /**
     * Creates a new issuer instance initialized with provided key pair
     * 
     * @param keyPair
     * @return
     */
    Issuer createIssuer(KeyPair keyPair);
    
//TODO    JsonObject derive(JsonObject document, DocumentLoader loader);
}
