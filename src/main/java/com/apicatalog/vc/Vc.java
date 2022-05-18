package com.apicatalog.vc;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.loader.DocumentLoader;

import jakarta.json.JsonArray;

/**
 * High level API to process Verified Credentials
 *
 */
public final class Vc {

    public static boolean verify(String input, DocumentLoader loader) throws VerificationError {
        
        try {
            JsonArray expanded = JsonLd.expand(input).loader(loader).get();
        
            // TODO Auto-generated method stub
            return true;

        } catch (JsonLdError e) {
            e.printStackTrace();
            throw new VerificationError();
        }
    }
  
}
