package com.apicatalog.trust.semantic;

import java.util.Collection;
import java.util.Map;

import com.apicatalog.trust.Document;

public interface GraphAdapter extends Document.Adapter {

    @FunctionalInterface
    public interface Factory {
        GraphAdapter createProcessor(
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

    Map<String, Object> expandedData();
}
