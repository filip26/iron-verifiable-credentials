package com.apicatalog.trust.processor;

import java.util.Collection;
import java.util.Map;

import com.apicatalog.trust.model.SemanticModel;
import com.apicatalog.trust.payload.PayloadGenerator;

public interface GraphProcessor extends DocumentProcessor {

    @FunctionalInterface
    public interface Factory {
        GraphProcessor createProcessor(
                SemanticModel model,
                Collection<String> context,
                Map<String, Object> document);
    }

    Collection<String> context();

    Collection<String[]> data();

    // returns proof graph ids, might be URI or blank node identifier
    Collection<String> proofs();

    Collection<String[]> proof(String graph);

    String proofType(String graph);

    PayloadGenerator createPayload();
}
