package com.apicatalog.trust.semantic;

import java.util.Collection;
import java.util.Map;

import com.apicatalog.trust.Document;
import com.apicatalog.trust.model.ProcessingModel.Vocab;

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

    Vocab keys();
    
    Map<String, ?> compacted();
}
