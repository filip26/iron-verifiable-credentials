package com.apicatalog.vc.proof;

import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.model.DocumentModel;

public interface DerivedProofValue extends ProofValue {

    DocumentModel documentModel();
    
}
