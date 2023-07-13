package com.apicatalog.vc.method;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.VerificationMethod;

import jakarta.json.JsonObject;

public interface VerificationMethodProcessor {

    /**
     * Provides an external JSON-LD context URI defying the proof type. The context
     * URI is used to expand the deferred proof verification method.
     * 
     * @return JSON-LD context URI or <code>null</code> (default)
     */
    default String context() {
        return null;
    }

    /**
     * Deserialize the given expanded JSON-LD object into a
     * {@link VerificationMethod}.
     * 
     * @param expanded JSON-LD object in an expanded form
     * 
     * @return a new {@link VerificationMethod} instance
     * @throws DocumentError if the given object cannot be deserialized
     */
    VerificationMethod readMethod(JsonObject expanded) throws DocumentError;
}
