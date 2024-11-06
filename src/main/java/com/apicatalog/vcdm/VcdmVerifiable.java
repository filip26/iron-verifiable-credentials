package com.apicatalog.vcdm;

import com.apicatalog.vc.Verifiable;

/**
 * A common ancestor to all W3C VCDM based verifiables.
 */
public interface VcdmVerifiable extends Verifiable {

    /**
     * Verifiable credentials data model version.
     * 
     * @return the data model version, never <code>null</code>
     */
    VcdmVersion version();
}
