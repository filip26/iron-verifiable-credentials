package com.apicatalog.ld.signature.json;

import java.net.URI;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.method.VerificationMethod;

import jakarta.json.JsonObject;

public interface MethodAdapter {

    /**
     * An adapter type 
     * @return an absolute URI identifying the method JSON-LD type
     */
    String type();
    
    /**
     * Transforms the given JSON object into a verification method, verification key, or a key pair 
     * @param object
     * @return
     * @throws DocumentError
     */
    VerificationMethod deserialize(JsonObject object) throws DocumentError;

    JsonObject serialize(VerificationMethod proof) throws DocumentError;

    /**
     * Get an optional JSON-LD context used when expanding remote verification method fetched on-demand.
     * 
     * @return an {@link URI} referencing a JSON-LD context or <code>null</code> if a context is embedded or not needed
     */
    URI contextFor(URI id);
    
    //TODO URI getBase(URI id);
}
