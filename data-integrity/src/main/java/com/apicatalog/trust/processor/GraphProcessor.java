package com.apicatalog.trust.processor;

import java.util.Collection;
import java.util.Map;

import com.apicatalog.trust.model.SemanticModel;

public interface GraphProcessor extends PayloadProcessor {

    @FunctionalInterface
    public interface Factory {
        GraphProcessor newInstance(
                SemanticModel model,
                Collection<String> context,
                Map<String, Object> document);
    }

    Collection<String> contexts();

    Collection<String[]> proof(String graph);

    Collection<String> proofs();

    String proofType(String graph);

    /**
     * resets the selector state, but might cache vanilla digestible payload for
     * re-use
     * 
     */
    void reset();
}
