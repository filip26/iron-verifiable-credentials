package com.apicatalog.vc.model;

import java.util.Collection;

import com.apicatalog.ld.DocumentError;

public interface DocumentModel {

    VerifiableMaterial data();

    Collection<VerifiableMaterial> proofs();
    
    VerifiableMaterial materialize() throws DocumentError;

    DocumentModel of(VerifiableMaterial data, Collection<VerifiableMaterial> proofs);
}
