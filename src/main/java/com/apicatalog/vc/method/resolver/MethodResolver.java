package com.apicatalog.vc.method.resolver;

import com.apicatalog.controller.key.VerificationKey;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.proof.Proof;

public interface MethodResolver {

    /**
     * Get {@link VerificationKey} for the given {@link Proof}. 
     *
     * @param proof a proof to verify
     * 
     * @return {@link VerificationKey} instance
     * 
     * @throws DocumentError
     */
    VerificationKey resolve(Proof proof) throws DocumentError;

}
