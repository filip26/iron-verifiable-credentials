package com.apicatalog.vc.model;

import java.util.Collection;

import com.apicatalog.ld.DocumentError;

import jakarta.json.JsonObject;

@Deprecated
public interface VerifiableReaderProvider {

    /**
     * Get a reader that can process the given document and contexts
     * 
     * @param contexts extracted or injected JSON-LD contexts
     * @param document JSON-LD in a compacted form to read as a verifiable document
     * 
     * @return a verifiable reader or <code>null</code> if this provider does not
     *         recognize the input
     * 
     * @throws DocumentError in a case a document model version is malformed
     */
    VerifiableReader reader(Collection<String> contexts, JsonObject document) throws DocumentError;

}
