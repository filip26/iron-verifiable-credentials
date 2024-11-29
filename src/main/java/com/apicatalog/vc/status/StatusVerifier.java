package com.apicatalog.vc.status;

import com.apicatalog.vc.Credential;
import com.apicatalog.vc.model.DocumentError;

/**
 * Allows to implement a custom verifiable credential status verifier
 *
 */
public interface StatusVerifier {

    /**
     * Verify the given credential status
     * 
     * @param credential
     * @param status
     * @throws DocumentError
     */
    void verify(
            Credential credential,
            Status status) throws DocumentError;

}