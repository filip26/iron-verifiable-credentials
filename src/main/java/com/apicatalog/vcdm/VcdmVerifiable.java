package com.apicatalog.vcdm;

import com.apicatalog.vc.Verifiable;

public interface VcdmVerifiable extends Verifiable {

//    @Override
//    default Collection<Proof> proofs() {
//        return Collections.emptyList();
//    }
    
    /**
     * Verifiable credentials data model version. Will be moved into a separate
     * interface specialized to VCDM.
     * 
     * @return the data model version, never <code>null</code>
     */
    VcdmVersion version();
}
