package com.apicatalog.trust.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.apicatalog.trust.model.SemanticModel;
import com.apicatalog.trust.model.SemanticModel.QuadConsumer;
import com.apicatalog.trust.payload.DigestiblePayload;
import com.apicatalog.trust.payload.GenericPayload;

public class StandardGraphProcessor implements GraphProcessor {

    private final SemanticModel model;
    private final Collection<String> context;
    private final Map<String, Object> document;

    private Dataset dataset;
    private Collection<String> includedProofs;

    public StandardGraphProcessor(
            SemanticModel model,
            Collection<String> context,
            Map<String, Object> document) {
        this.model = model;
        this.context = context;
        this.document = document;

        this.includedProofs = null;
    }

    @Override
    public Collection<String> contexts() {
        return context;
    }

    @Override
    public Collection<String[]> proof(String graph) {
        lazyInit();
        return dataset.graphs.get(graph);
    }
    
    
    public Collection<String> proofs() {
        lazyInit();
        return dataset.proofGraphs;
    }

    @Override
    public String proofType(String graph) {
        lazyInit();
        return dataset.proofTypes.get(graph);
    }

    @Override
    public DigestiblePayload digestible() {

        lazyInit();
        
        var canonizer = model.newCanonizer();
        var consumer = canonizer.consumer();

        Set<String> selectedGraph = Set.of();

        if (includedProofs != null && !includedProofs.isEmpty()) {
            selectedGraph = new HashSet<String>();

            // select proofs
            for (var graph : dataset.graphs.entrySet()) {
                if ("@default".equals(graph.getKey())) {
                    continue;
                }
                if (includedProofs.contains(graph.getValue().iterator().next()[0])) {
                    selectedGraph.add(graph.getKey());
                    for (var quad : graph.getValue()) {
                        consumer.accept(quad[0], quad[1], quad[2], quad[3], quad[4], quad[5], quad[6]);
                    }
                }
            }
        }

        for (var quad : dataset.graphs.get("@default")) {
            if (!"https://w3id.org/security#proof".equals(quad[1])
                    || selectedGraph.contains(quad[2])) {
                consumer.accept(quad[0], quad[1], quad[2], quad[3], quad[4], quad[5], null);
            }
        }

        var canonical = canonizer.canonize();

        return new GenericPayload(canonical);
    }

    @Override
    public void withProofs(Collection<String> ids) {
        this.includedProofs = ids;
    }

    @Override
    public void reset() {
        this.includedProofs = null;
    }

    private void lazyInit() {
        if (dataset == null) {

            dataset = new Dataset();

            model.tordf().accept(document, dataset);
        }
    }

    private static class Dataset implements QuadConsumer {

        private final Map<String, String> proofTypes = new HashMap<>();

        private Map<String, Collection<String[]>> graphs = new HashMap<>();

        private Collection<String> proofGraphs = new HashSet<>();

        @Override
        public void accept(
                String subject,
                String predicate,
                String object,
                String datatype,
                String language,
                String direction,
                String graph) {

            var key = graph;

            if (key == null) {
                key = "@default";

                if ("https://w3id.org/security#proof".equals(predicate)) {
                    proofGraphs.add(object);
                }

            } else if ("http://www.w3.org/1999/02/22-rdf-syntax-ns#type".equals(predicate)) {
                proofTypes.put(graph, object);
            }

            graphs.computeIfAbsent(key, (_) -> new ArrayList<String[]>())
                    .add(new String[] {
                            subject, predicate, object, datatype, language, direction, graph
                    });
        }
    }
}
