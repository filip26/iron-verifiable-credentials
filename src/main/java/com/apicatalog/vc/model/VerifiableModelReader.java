package com.apicatalog.vc.model;

import com.apicatalog.ld.DocumentError;

public interface VerifiableModelReader {

    /**
     * Extract verifiable data model.
     * 
     * @param material
     * @return a verifiable data model
     * 
     * @throws DocumentError
     * 
     */
    VerifiableModel read(VerifiableMaterial material) throws DocumentError;
    
}
