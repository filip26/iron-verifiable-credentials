package com.apicatalog.vc.model;

import java.util.Collection;

import com.apicatalog.ld.DocumentError;

public interface VerifiableModel {

    VerifiableMaterial data();

    Collection<VerifiableMaterial> proofs();
    
    VerifiableMaterial materialize() throws DocumentError;

    VerifiableModel withProof(VerifiableMaterial signedProof);
}
