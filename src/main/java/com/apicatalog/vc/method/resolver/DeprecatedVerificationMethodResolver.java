package com.apicatalog.vc.method.resolver;

import java.net.URI;

import com.apicatalog.controller.key.VerificationKey;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.proof.Proof;

public interface DeprecatedVerificationMethodResolver {

    /**
     * Resolve the given {@link URI} into {@link VerificationKey}
     *
     * @param id    an {@link URI} to resolve
     * @param proof a proof to which the verification method is bound to
     * 
     * @return {@link VerificationKey} instance
     * 
     * @throws DocumentError
     */
    VerificationKey resolve(URI id, Proof proof) throws DocumentError;

    boolean isAccepted(URI id);
}
