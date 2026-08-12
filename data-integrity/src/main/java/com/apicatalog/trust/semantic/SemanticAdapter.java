package com.apicatalog.trust.semantic;

import java.util.Collection;
import java.util.Map;

import com.apicatalog.trust.Document;
import com.apicatalog.trust.model.Model.Vocab;
import com.apicatalog.trust.semantic.Graph.Node;

public interface SemanticAdapter extends Document.Adapter {

    @FunctionalInterface
    public interface Factory {
        SemanticAdapter createAdapter(
                SemanticModel model,
                Collection<String> context,
                Map<String, Object> document);
    }

    Collection<String> context();

    Graph data();

//    Node proof(String id);

    // returns proof graph ids, might be URI or blank node identifier
    Collection<String> proofGraphs();

    Graph.Node proofGraph(String graph);

    Map<String, Object> expandedData();

    Vocab vocab();

    Map<String, ?> source();
}
