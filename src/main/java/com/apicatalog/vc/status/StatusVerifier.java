package com.apicatalog.vc.status;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.Credential;

/**
 * Allows to implement a custom verifiable credential status verifier
 *
 */
public interface StatusVerifier {

    /**
     * Verify the given credential status in an expanded JSON-LD form
     * 
     * @param credential
     * @param status
     * @throws DocumentError
     */
    void verify( 
            Credential credential,
            Status status
            ) throws DocumentError;

}