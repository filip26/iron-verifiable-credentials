package com.apicatalog.vc.suite;

import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.vc.issuer.Issuer;
import com.apicatalog.vc.proof.ProofAdapter;

/**
 * A container providing primitives to process a proof.
 */
public interface SignatureSuite {

    /**
     * Provides an adapter to process a proof type and related types
     * 
     * @return a proof adapter, never <code>null</code>
     */
    ProofAdapter proofAdapter();
    
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
     * @param proofType an URI representing a proof JSON-LD type
     * @param proof     a proof in an expanded JSON-LD form
     * 
     * @return <code>true</code> if the proof is supported, <code>false</code>
     *         otherwise
     */
//    boolean isSupported(String proofType, LinkedNode proof);

    /**
     * Deserialize the given expanded JSON-LD object into a {@link Proof}.
     * 
     * @param proof JSON-LD object in an expanded form
     * 
     * @return a new {@link Proof} instance
     * @throws DocumentError if the given object cannot be deserialized
     */
//    Proof getProof(LinkedNode proof, DocumentLoader loader) throws DocumentError;

}
