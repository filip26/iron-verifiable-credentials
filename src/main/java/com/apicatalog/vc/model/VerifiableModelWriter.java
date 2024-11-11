package com.apicatalog.vc.model;

import com.apicatalog.ld.DocumentError;

@FunctionalInterface
public interface VerifiableModelWriter {

    /**
     * Serializes verifiable data model into a verifiable material.
     * 
     * @param model
     * @return a verifiable material
     * 
     * @throws DocumentError
     * 
     */
    VerifiableMaterial write(VerifiableModel model) throws DocumentError;

}
