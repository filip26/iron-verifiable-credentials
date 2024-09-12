package com.apicatalog.vc.proof;

/**
 * A container holding one or more proofs.
 */
public interface Proofs extends Iterable<Proof> {

    /**
     * Verify all proofs. A verification strategy, e.g. proof chain, proof set,
     * is a subject to a particular implementation.
     */
    void verify();

}
