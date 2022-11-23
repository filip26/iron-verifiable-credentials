package com.apicatalog.ld.signature.json;

import java.net.URI;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.proof.VerificationMethod;

import jakarta.json.JsonObject;

public interface VerificationMethodJsonAdapter {

    /**
     * Returns <code>true</code> if the adapter supports (can (de)serialize the given method type
     * 
     * @param type an {@link URI} representing a verification method type
     * @return <code>true</code> if the given type is supported by the adapter, otherwise <code>false</code> 
     */
    boolean isSupportedType(String type);
    VerificationMethod deserialize(JsonObject object) throws DocumentError;

    JsonObject serialize(VerificationMethod proof) throws DocumentError;

    /**
     * Get an optional JSON-LD context used when expanding remote verification method fetched on-demand.
     * 
     * @return an {@link URI} referencing a JSON-LD context or <code>null</code> if a context is embedded or not needed
     */
    URI getContextFor(URI id);

}
