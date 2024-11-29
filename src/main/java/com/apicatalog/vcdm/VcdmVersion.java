package com.apicatalog.vcdm;

import java.util.Collection;

import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.vc.model.DocumentError;
import com.apicatalog.vc.model.DocumentError.ErrorType;

/**
 * W3C Verifiable Credentials Data Model Version (VCDM)
 */
public enum VcdmVersion {

    /**
     * Data Model Version 1.1
     * 
     * @see <a href="https://www.w3.org/TR/vc-data-model/">Specification</a>
     */
    V11,

    /**
     * Data Model Version 2.0
     * 
     * @see <a href="https://w3c.github.io/vc-data-model/">Specification</a>
     */
    V20;
    
    public static VcdmVersion of(final Collection<String> contexts) throws DocumentError {

        if (contexts == null || contexts.isEmpty()) {
            return null;
        }

        final String firstContext = contexts.iterator().next();

        if (VcdmVocab.CONTEXT_MODEL_V1.equals(firstContext)) {
            return VcdmVersion.V11;
        }
        if (VcdmVocab.CONTEXT_MODEL_V2.equals(firstContext)) {
            return VcdmVersion.V20;
        }

        for (final String context : contexts) {
            if (VcdmVocab.CONTEXT_MODEL_V1.equals(context)
                    || VcdmVocab.CONTEXT_MODEL_V2.equals(context)) {

                throw new DocumentError(ErrorType.Invalid, Keywords.CONTEXT);
            }
        }
        return null;
    }
}
