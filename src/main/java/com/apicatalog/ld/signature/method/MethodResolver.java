package com.apicatalog.ld.signature.method;

import java.net.URI;

import com.apicatalog.did.document.DidDocument;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.SignatureSuite;

public interface MethodResolver {

    /**
     * Resolves the given {@link URI} into {@link VerificationMethod}
     *
     * @param id an {@link URI} To resolve as a verification method
     * @param loader 
     * @param suite a suite (a proof type) context
     * @return The new {@link DidDocument}
     * 
     * @throws DocumentError
     */
    VerificationMethod resolve(URI id, DocumentLoader loader, SignatureSuite suite) throws DocumentError;
    
    boolean isAccepted(URI id);
    
}
