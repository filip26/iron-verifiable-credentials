package com.apicatalog.vc.processor;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.vc.model.Proof;

class ProofQueue {

    final Collection<Proof> proofs;
    final Set<URI> ids;
    
    protected ProofQueue(Collection<Proof> proofs) {
        this.proofs = new ArrayList<>(proofs);
        this.ids = new HashSet<>(proofs.size());
    }
    
    public static final ProofQueue create(Collection<Proof> proofs) {
        return new ProofQueue(proofs);
    }
    
    public boolean isEmpty() {
        return !proofs.isEmpty();
    }

    public Proof pop() throws DocumentError {
        if (proofs.isEmpty()) {
            return null;
        }
        
        for (Proof proof : proofs) {
            if (proof.previousProof() == null
                    || ids.contains(proof.previousProof())
                    ) {
                if (proof.id() != null) {
                    if (ids.contains(proof.id())) {
                        throw new DocumentError(ErrorType.Invalid, "ProofId");
                    }
                    ids.add(proof.id());
                }
                proofs.remove(proof);
                return proof;
            }
        }
        throw new DocumentError(ErrorType.Invalid, "PreviousProofId");
    }
}
