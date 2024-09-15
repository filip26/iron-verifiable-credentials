package com.apicatalog.vcdm;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.proof.Proof;

public abstract class VcdmVerifiable implements Verifiable {

    protected URI id;
    protected Collection<Proof> proofs;

    @Override
    public URI id() {
        return id;
    }

    @Override
    public Collection<Proof> proofs() {
        return proofs;
    }
    
    public void proofs(Collection<Proof> proofs) {
        this.proofs = proofs;
    }

    /**
     * Verifiable credentials data model version. Will be moved into a separate
     * interface specialized to VCDM.
     * 
     * @return the data model version, never <code>null</code>
     */
    public abstract VcdmVersion version();
}
