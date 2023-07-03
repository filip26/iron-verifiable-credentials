package com.apicatalog.vc.method.resolver;

import java.net.URI;

import com.apicatalog.did.document.DidDocument;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.vc.model.Proof;
import com.apicatalog.vc.suite.SignatureSuite;

public interface MethodResolver {

    /**
     * Resolves the given {@link URI} into {@link VerificationMethod}
     *
     * @param id     an {@link URI} To resolve as a verification method
     * @param loader
     * @param proof a proof to be verified  
     * @return The new {@link DidDocument}
     * 
     * @throws DocumentError
     */
    VerificationMethod resolve(URI id, DocumentLoader loader, Proof proof) throws DocumentError;

    boolean isAccepted(URI id);
}
