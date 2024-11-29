package com.apicatalog.vc.verifier;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.apicatalog.vc.model.DocumentError;
import com.apicatalog.vc.model.DocumentError.ErrorType;
import com.apicatalog.vc.proof.Proof;

class ProofQueue {

    final Collection<Proof> proofs;
    final Set<URI> ids;
    final boolean[] removed;

    protected ProofQueue(Collection<Proof> proofs) {
        this.proofs = proofs;
        this.ids = new HashSet<>(proofs.size());
        this.removed = new boolean[proofs.size()];
    }

    public static final ProofQueue create(Collection<Proof> proofs) {
        return new ProofQueue(proofs);
    }

    public boolean isEmpty() {
        return !proofs.isEmpty();
    }

    public static Collection<Proof> sort(Collection<Proof> proofs) throws DocumentError {

        if (proofs == null || proofs.isEmpty()) {
            return Collections.emptyList();
        }

        if (proofs.size() == 1) {
            return proofs;
        }

        Collection<Proof> sorted = new ArrayList<>(proofs.size());

        final ProofQueue queue = ProofQueue.create(proofs);

        for (int i = 0; i < proofs.size(); i++) {
            sorted.add(queue.pop());
        }

        return sorted;
    }

    public Proof pop() throws DocumentError {

        if (proofs.isEmpty()) {
            return null;
        }

        int index = 0;

        for (final Proof proof : proofs) {
            if (!removed[index] &&
                    (proof.previousProof() == null
                            || proof.previousProof().isEmpty()
                            || proof.previousProof().stream().allMatch(ids::contains))) {
                if (proof.id() != null) {

                    if (ids.contains(proof.id())) {
                        throw new DocumentError(ErrorType.Invalid, "ProofId");
                    }
                    ids.add(proof.id());
                }

                removed[index] = true;
                return proof;
            }
            index++;
        }
        throw new DocumentError(ErrorType.Invalid, "ProofChain");
    }
}
