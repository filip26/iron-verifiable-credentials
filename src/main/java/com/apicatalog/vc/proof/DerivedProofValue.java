package com.apicatalog.vc.proof;

import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.model.VerifiableModel;

public interface DerivedProofValue extends ProofValue {

    VerifiableModel documentModel();
    VerifiableMaterial derivedProof();
    
}
