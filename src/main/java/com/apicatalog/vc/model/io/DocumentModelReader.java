package com.apicatalog.vc.model.io;

import com.apicatalog.vc.model.DocumentError;
import com.apicatalog.vc.model.DocumentModel;
import com.apicatalog.vc.model.VerifiableMaterial;

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
