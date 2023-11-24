package com.apicatalog.vc.method;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.node.LdAdapter;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.vc.model.Proof;

import jakarta.json.JsonObject;

public interface MethodAdapter extends LdAdapter<VerificationMethod> {

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
     * @param document JSON-LD object in an expanded form
     * 
     * @return a new {@link VerificationMethod} instance
     * @throws DocumentError if the given object cannot be deserialized
     */
    @Override
    VerificationMethod read(JsonObject document) throws DocumentError;
}
