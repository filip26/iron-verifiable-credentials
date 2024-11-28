package com.apicatalog.vc.model;

import java.net.URI;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.VerifiableDocument;

@FunctionalInterface
public interface DocumentAdapter {

    /**
     * Materialize verifiable material into a verifiable instance.
     * 
     * @param model
     * @param loader
     * @param base
     * @return a verifiable instance
     * 
     * @throws DocumentError
     * 
     */
    VerifiableDocument materialize(DocumentModel model, DocumentLoader loader, URI base) throws DocumentError;
}
