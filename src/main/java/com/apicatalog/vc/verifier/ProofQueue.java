package com.apicatalog.vc.verifier;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.vc.proof.Proof;

class ProofQueue {

    final Collection<Proof> proofs;
    final Set<URI> ids;

    protected ProofQueue(Collection<Proof> proofs) {
        this.proofs = proofs;
        this.ids = new HashSet<>(proofs.size());
    }

    public static final ProofQueue create(Collection<Proof> proofs) {
        return new ProofQueue(new ArrayList<>(proofs));
    }

    public boolean isEmpty() {
        return !proofs.isEmpty();
    }

    public static Collection<Proof> sort(Collection<Proof> proofs) throws DocumentError {
        System.out.println(proofs.size());
        if (proofs == null || proofs.isEmpty()) {
            return Collections.emptyList();
        }

        if (proofs.size() == 1) {
            return proofs;
        }

        Collection<Proof> sorted = new ArrayList<>(proofs.size());

        final ProofQueue queue = ProofQueue.create(proofs);

        for (int i = 0; i < proofs.size(); i++) {
//            sorted.add(queue.pop());
        }

        return sorted;
    }

    public Proof pop() throws DocumentError {

        if (proofs.isEmpty()) {
            return null;
        }

        for (Proof proof : proofs) {
            System.out.println(proof.id());
            if (proof.previousProof() == null
                    || ids.contains(proof.previousProof())) {
                if (proof.id() != null) {
                    if (ids.contains(proof.id())) {
                        System.out.println(proof.id());
                        throw new DocumentError(ErrorType.Invalid, "ProofId");
                    }
                    ids.add(proof.id());
                }
                proofs.remove(proof);
                return proof;
            }
        }
        throw new DocumentError(ErrorType.Invalid, "ProofChain");
    }
}
