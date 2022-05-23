package com.apicatalog.vc;

import java.net.URI;

import com.apicatalog.jsonld.loader.DocumentLoader;

/**
 * High level API to process Verified Credentials and Verified Presentations.
 *
 */
public final class Vc {

    /**
     * Verifies VC/VP document data integrity and signature.
     * 
     * @param location
     * @param loader
     * @throws DataIntegrityError
     * @throws VerificationError
     */
    public static /*FIXME use VerificationApi*/ void verify(URI location, DocumentLoader loader) throws DataIntegrityError, VerificationError {

        final VcDocument data  = VcDocument.load(location, loader);

        if (data == null || !data.isVerifiable()) {
            throw new VerificationError();                  //TODO
        }

        data.asVerifiable().verify();
    }
}
