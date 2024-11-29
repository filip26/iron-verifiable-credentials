package com.apicatalog.vc.model.adapter;

import java.net.URI;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.vc.VerifiableDocument;
import com.apicatalog.vc.model.DocumentError;
import com.apicatalog.vc.model.DocumentModel;

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
