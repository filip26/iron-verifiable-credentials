package com.apicatalog.vc.suite;

import com.apicatalog.controller.key.KeyPair;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.issuer.Issuer;
import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.proof.Proof;

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
     * @param proof
     * 
     * @return <code>true</code> if the proof is supported, <code>false</code>
     *         otherwise
     */
    boolean isSupported(VerifiableMaterial verifiable, VerifiableMaterial proof);

    /**
     * Deserialize the given expanded JSON-LD object into a {@link Proof}.
     * 
     * @param verifiable a verifiable to which the proof is bound to
     * @param proof
     * @param loader
     * 
     * @return a new {@link Proof} instance
     * @throws DocumentError if the given object cannot be deserialized
     */
    // TODO base
    Proof getProof(VerifiableMaterial verifiable, VerifiableMaterial proof, DocumentLoader loader) throws DocumentError;

}
