package com.apicatalog.vc.model.io;

import com.apicatalog.vc.model.DocumentError;
import com.apicatalog.vc.model.DocumentModel;
import com.apicatalog.vc.model.VerifiableMaterial;

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
