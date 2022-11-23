package com.apicatalog.ld.signature.proof;

import java.net.URI;

import com.apicatalog.did.document.DidDocument;

public interface MethodResolver {

    /**
     * Resolves the given {@link URI} into {@link VerificationMethod}
     *
     * @param did To resolve
     * @return The new {@link DidDocument}
     */
    VerificationMethod resolve(URI uri);
    
}
