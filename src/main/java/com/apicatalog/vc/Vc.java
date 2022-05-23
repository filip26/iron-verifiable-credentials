package com.apicatalog.vc;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.loader.DocumentLoader;

import jakarta.json.JsonArray;

/**
 * High level API to process Verified Credentials and Verified Presentations
 *
 */
public final class Vc {


    
    public static void verify(String location, DocumentLoader loader) throws DataIntegrityError, VerificationError {

        try {
            // VC/VP in expanded form
            final JsonArray expanded = JsonLd.expand(location).loader(loader).get();

            if (expanded == null || expanded.isEmpty()) {
                throw new VerificationError();                  //TODO
            }

            StructuredData data  = StructuredData.from(expanded);
            
            if (data == null || !data.isVerifiable()) {
                throw new VerificationError();                  //TODO
            }

            data.asVerifiable().verify();

        } catch (JsonLdError e) {
            throw new VerificationError(e);
        }
    }
}
