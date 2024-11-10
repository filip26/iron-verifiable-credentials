package com.apicatalog.vc.model;

import java.net.URI;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.Verifiable;

@FunctionalInterface
public interface VerifiableAdapter {

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
    Verifiable materialize(VerifiableModel model, DocumentLoader loader, URI base) throws DocumentError;
}
