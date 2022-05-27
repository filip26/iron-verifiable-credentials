package com.apicatalog.vc;

import java.net.URI;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.lds.SignatureSuite;

import jakarta.json.JsonObject;

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
    public static /*FIXME use VerificationApi, make loader optional - use default*/ void verify(URI location, DocumentLoader loader) throws DataIntegrityError, VerificationError {

        final VcDocument data  = VcDocument.load(location, loader);

        if (data == null || !data.isVerifiable()) {
            throw new VerificationError();                  //TODO
        }

        data.asVerifiable().verify();
    }

    /**
     * Signs VC/VP document with using the provided signature suite. 

     * @param location
     * @param suite
     * @param loader
     * @return signed VC/VP with proof property at the root level
     */
    public static JsonObject issue(URI location, SignatureSuite suite, DocumentLoader loader) {

        //TODO
        
        return JsonObject.EMPTY_JSON_OBJECT;
    }
}
