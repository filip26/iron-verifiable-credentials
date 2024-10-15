package com.apicatalog.vcdm;

import java.util.Collection;
import java.util.Collections;

import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.proof.Proof;

public interface VcdmVerifiable extends Verifiable {

    @Override
    default Collection<Proof> proofs() {
        return Collections.emptyList();
    }
    
    /**
     * Verifiable credentials data model version. Will be moved into a separate
     * interface specialized to VCDM.
     * 
     * @return the data model version, never <code>null</code>
     */
    VcdmVersion version();
}
