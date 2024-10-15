package com.apicatalog.vc.proof;

import java.util.Collection;
import java.util.Collections;

import com.apicatalog.linkedtree.orm.proxy.ObjectProvider;

public class ProofProvider implements ObjectProvider<Collection<Proof>> {

    Collection<Proof> proofs;
        
    @Override
    public void accept(Collection<Proof> proofs) {
        this.proofs = proofs;
    }

    @Override
    public Collection<Proof> get() {
        return proofs == null
                ? Collections.emptyList()
                : proofs;
    }

}
