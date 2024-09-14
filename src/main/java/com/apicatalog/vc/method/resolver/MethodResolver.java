package com.apicatalog.vc.method.resolver;

import java.net.URI;

import com.apicatalog.did.document.DidDocument;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.vc.proof.Proof;

public interface MethodResolver {

    /**
     * Resolves the given {@link URI} into {@link VerificationMethod}
     *
     * @param id an {@link URI} To resolve as a verification method
     * 
     * @return the new {@link VerificationMethod} instance
     * 
     * @throws DocumentError
     */
    VerificationMethod resolve(URI id) throws DocumentError;

    boolean isAccepted(URI id);
}
