package com.apicatalog.vc.adapter;

import java.net.URI;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.vc.model.DocumentError;
import com.apicatalog.vc.model.DocumentModel;
import com.apicatalog.vc.proof.Proof;

@FunctionalInterface
public interface ProofAdapter {

    /**
     * Materialize document model.
     * 
     * @param model
     * @param loader
     * @param base
     * @return a new instance
     * 
     * @throws DocumentError
     * 
     */
    Proof materialize(DocumentModel model, DocumentLoader loader, URI base) throws DocumentError;
}
