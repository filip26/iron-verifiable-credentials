package com.apicatalog.vc.proof;

import com.apicatalog.controller.key.VerificationKey;
import com.apicatalog.cryptosuite.VerificationError;
import com.apicatalog.vc.model.DocumentError;

public interface ProofValue {

    /**
     * Get a proof instance to which this proof value belongs to.
     * 
     * @return a proof instance
     */
    Proof proof();
    
    /**
     * Cryptographically verify the proof value.
     *  
     * @param key
     * @throws VerificationError
     * @throws DocumentError
     */
    void verify(VerificationKey key) throws VerificationError, DocumentError;

}
