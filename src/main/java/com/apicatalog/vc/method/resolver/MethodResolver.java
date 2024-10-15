package com.apicatalog.vc.method.resolver;

import java.net.URI;

import com.apicatalog.controller.method.VerificationMethod;
import com.apicatalog.ld.DocumentError;

public interface MethodResolver {

    /**
     * Resolve the given {@link URI} into {@link VerificationMethod}
     *
     * @param id      an {@link URI} to resolve as a verification method
     * @param purpose a purpose of the requested verification method
     * 
     * @return a new {@link VerificationMethod} instance
     * 
     * @throws DocumentError
     */
    VerificationMethod resolve(URI id, URI purpose) throws DocumentError;

    boolean isAccepted(URI id);
}
