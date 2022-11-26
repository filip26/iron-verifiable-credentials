package com.apicatalog.ld.signature.adapter;

import java.net.URI;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.method.VerificationMethod;

import jakarta.json.JsonObject;

public interface MethodAdapter {

    /**
     * An adapter type 
     * @return an absolute URI identifying the method JSON-LD type
     */
    URI type();
    
    /**
     * Transforms the given JSON object into a verification method, verification key, or a key pair 
     * @param object
     * @return
     * @throws DocumentError
     */
    VerificationMethod deserialize(JsonObject object) throws DocumentError;

    JsonObject serialize(VerificationMethod proof) throws DocumentError;
}
