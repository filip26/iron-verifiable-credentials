package com.apicatalog.trust.semantic;

import java.util.Map;
import java.util.SequencedCollection;

public class GraphProcessorFactory {

    final GraphProcessorResources resources;

    final String typePredicate;
    final String proofPredicate;

    private GraphProcessorFactory(
            GraphProcessorResources resources,
            String typePredicate,
            String proofPredicate) {
        this.resources = resources;
        this.typePredicate = typePredicate;
        this.proofPredicate = proofPredicate;
    }

    public GraphProcessor newInstance(SequencedCollection<?> context, Map<String, ?> document) {

        
        
        return null;
    }

}
