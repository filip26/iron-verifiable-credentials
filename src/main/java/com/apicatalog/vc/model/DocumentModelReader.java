package com.apicatalog.vc.model;

import com.apicatalog.ld.DocumentError;

@FunctionalInterface
public interface DocumentModelReader {

    /**
     * Extract verifiable data model.
     * 
     * @param data
     * @return a verifiable data model
     * 
     * @throws DocumentError
     * 
     */
    DocumentModel read(VerifiableMaterial data) throws DocumentError;

}
