package com.apicatalog.vc.model;

import com.apicatalog.ld.DocumentError;

@FunctionalInterface
public interface VerifiableModelReader {

    /**
     * Extract verifiable data model.
     * 
     * @param data
     * @return a verifiable data model
     * 
     * @throws DocumentError
     * 
     */
    VerifiableModel read(VerifiableMaterial data) throws DocumentError;

}
