package com.apicatalog.trust.semantic;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import com.apicatalog.trust.model.Model.Vocab;
import com.apicatalog.trust.proof.ProofCursor;
import com.apicatalog.trust.semantic.SemanticModel.QuadConsumer;

public final class GraphAccessor implements SemanticModel.Accessor {

    private final SemanticModel model;

    private final Collection<String> context;
    private final Map<String, Object> document;

    private Map<String, Object> expandedData;
    private Collection<?> expandedProofs;

    private Dataset dataset;

    protected GraphAccessor(
            SemanticModel model,
            Collection<String> context,
            Map<String, Object> document) {
        this.model = model;
        this.context = context;
        this.document = document;

        this.expandedData = null;
        this.expandedProofs = null;

        this.dataset = null;
    }

    public static GraphAccessor newInstance(
            SemanticModel model,
            Collection<String> context,
            Map<String, Object> document) {
        return new GraphAccessor(model, context, document);
    }

    @Override
    public ProofCursor createProofCursor() {
        return model.createCursor(this);
    }

    @Override
    public Collection<String> context() {
        return context;
    }

    @Override
    public Graph data() {
        lazyInit();
        return dataset.graphs.get("@default");
    }

    @Override
    public Graph proofGraph(String graph) {
        lazyInit();
        return dataset.graphs.get(graph);
    }

    public Collection<String> proofGraphs() {
        lazyInit();
        return dataset.proofGraphs;
    }

    @Override
    public Map<String, Object> expandedData() {
        lazyInit();
        return expandedData;
    }

    private void lazyInit() {

        if (expandedData != null || dataset != null) {
            return;
        }

        // TODO get term map
        var expanded = model.expand().apply(document);

        if (expanded.size() != 1) {
            throw new IllegalArgumentException();
        }

        if (expanded.iterator().next() instanceof Map map) { // TODO use getFirst()
            expandedData = new LinkedHashMap<String, Object>(map);
            if (map.containsKey(model.vocab().proof())) {
                var proofs = expandedData.remove(model.vocab().proof());
                if (proofs instanceof Collection<?> col) {
                    expandedProofs = col;
                } else {
                    throw new IllegalStateException();
                }
            }

        } else {
            throw new IllegalArgumentException();
        }

//        if (expandedProofs != null) {
        dataset = new Dataset();
        dataset.proofPredicate = model.vocab().proof();
        model.tordf().accept(expanded, dataset);
//        }
//        if (dataset == null) {
//
//            dataset = new Dataset();
//            dataset.proofPredicate = model.vocab().proof();
//            dataset.typePredicate = model.vocab().type();
//
//            model.tordf().accept(document, dataset);
//        }
    }

    private static class Dataset implements QuadConsumer {

//        private final Map<String, String> proofTypes = new HashMap<>();

        private final Map<String, Graph> graphs = new HashMap<>();

        private final Collection<String> proofGraphs = new HashSet<>();

        private String proofPredicate;

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

            // default graph
            if (graph == null) {
                key = "@default";

                if (proofPredicate.equals(predicate)) {
                    proofGraphs.add(object);
                }
            }

            var container = graphs.computeIfAbsent(key, _ -> new Graph(graph, new HashMap<>()));

            var node = container.nodes().computeIfAbsent(
                    subject,
                    _ -> new Graph.Node(subject, graph));

//            if (typePredicate.equals(predicate)) {
//                node.type().add(object);
//            }

            node.addStatement(predicate, object, datatype, language, direction);
        }
    }

    @Override
    public Vocab vocab() {
        // FIXME read from JSON-LD term map after expansion
        return new Vocab("@context", "proof", "id", "type");
    }

    @Override
    public Map<String, ?> source() {
        return document;
    }
}
