package com.apicatalog.vc.model;

import java.net.URI;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.proof.Proof;

@FunctionalInterface
public interface ProofAdapter {

    /**
     * Materialize verifiable material into a proof.
     * 
     * @param model
     * @param loader
     * @param base
     * @return a verifiable instance
     * 
     * @throws DocumentError
     * 
     */
    Proof materialize(DocumentModel model, DocumentLoader loader, URI base) throws DocumentError;
}
