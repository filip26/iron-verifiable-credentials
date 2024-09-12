package com.apicatalog.vc.method;

import com.apicatalog.linkedtree.type.TypeAdapter;

public interface MethodAdapter extends TypeAdapter {

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
     * Deserialize the given {@link LinkedFragment} into a
     * {@link VerificationMethod}.
     * 
     * @param document
     * 
     * @return a new {@link VerificationMethod} instance
     * @throws DocumentError if the given object cannot be deserialized
     */
//    VerificationMethod read(LinkedFragment document) throws DocumentError;
    
}
