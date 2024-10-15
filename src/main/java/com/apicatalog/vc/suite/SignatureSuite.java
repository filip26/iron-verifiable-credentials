package com.apicatalog.vc.suite;

import com.apicatalog.controller.method.KeyPair;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.issuer.Issuer;
import com.apicatalog.vc.proof.Proof;

import jakarta.json.JsonObject;

/**
 * A container providing primitives to process a proof.
 */
public interface SignatureSuite {

    /**
     * Creates a new issuer instance initialized with provided key pair.
     * 
     * @param keyPair
     * @return a new issuer instance
     */
    default Issuer createIssuer(KeyPair keyPair) {
        throw new UnsupportedOperationException();
    }

    /**
     * Check if the given proof can be processed by the suite.
     * 
     * @param verifiable a verifiable to which the proof is bound to
     * @param proofType  an URI representing a proof JSON-LD type
     * @param proof      a proof in an expanded JSON-LD form
     * 
     * @return <code>true</code> if the proof is supported, <code>false</code>
     *         otherwise
     */
    boolean isSupported(Verifiable verifiable, String proofType, JsonObject proof);

    /**
     * Deserialize the given expanded JSON-LD object into a {@link Proof}.
     * 
     * @param verifiable a verifiable to which the proof is bound to
     * @param proof      JSON-LD object in an expanded form
     * @param loader
     * 
     * @return a new {@link Proof} instance
     * @throws DocumentError if the given object cannot be deserialized
     */
    Proof getProof(Verifiable verifiable, JsonObject proof, DocumentLoader loader) throws DocumentError;
}
