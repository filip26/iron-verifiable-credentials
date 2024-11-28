package com.apicatalog.vc.model;

import com.apicatalog.ld.DocumentError;

@FunctionalInterface
public interface DocumentModelWriter {

    /**
     * Serializes verifiable data model into a verifiable material.
     * 
     * @param model
     * @return a verifiable material
     * 
     * @throws DocumentError
     * 
     */
    VerifiableMaterial write(DocumentModel model) throws DocumentError;

}
