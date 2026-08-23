package com.apicatalog.trust.semantic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedCollection;

import com.apicatalog.trust.model.Model.Vocab;
import com.apicatalog.trust.proof.ProofCursor;
import com.apicatalog.trust.semantic.SemanticModel.QuadConsumer;

public final class GraphAccessor implements SemanticModel.Accessor {

    private final SemanticModel model;

    private final SequencedCollection<?> context;
    private final Map<String, ?> document;

    private final Map<String, ?> expandedData;

    private String resource;
    private final Collection<String> proofGraphs;
    private final Map<String, Graph> dataset;

    protected GraphAccessor(
            SemanticModel model,
            SequencedCollection<?> context,
            Map<String, ?> document,
            Map<String, Object> expandedData,
            String resource,
            Collection<String> proofGraphs,
            Map<String, Graph> dataset) {
        this.model = model;
        this.context = context;
        this.document = document;
        this.expandedData = expandedData;

        this.resource = resource;
        this.proofGraphs = proofGraphs;
        this.dataset = dataset;
    }

    public static GraphAccessor newInstance(
            SemanticModel model,
            SequencedCollection<?> context,
            Map<String, ?> document) {

        if (context == null || context.isEmpty()) {
            return null;
        }

        // TODO get term map
        var expanded = model.expand().apply(document);

        if (expanded.size() != 1) {
            throw new IllegalArgumentException();
        }

        Map<String, Object> expandedData = null;

        if (expanded.getFirst() instanceof Map map) {
            expandedData = new LinkedHashMap<String, Object>(map);
            if (map.containsKey(model.vocab().proof())) {
                var proofs = expandedData.remove(model.vocab().proof());
                if (proofs != null && !(proofs instanceof Collection)) {
                    throw new IllegalStateException();
                }
            }

        } else {
            throw new IllegalArgumentException();
        }

        var dataset = new DatasetBuilder();
        dataset.proofPredicate = model.vocab().proof();

        model.tordf().accept(expanded, dataset);

        return new GraphAccessor(
                model,
                context,
                document,
                expandedData,
                dataset.resource,
                dataset.proofGraphs,
                dataset.graphs);
    }

    public static GraphAccessor newInstance(
            SemanticModel model,
            Map<String, Graph> dataset) {

        var graph = dataset.get("@default");

        String resource = null;
        var proofGraphs = List.<String>of();

        if (graph.nodes().size() == 1) {
            var node = graph.nodes().firstEntry().getValue();
            resource = node.id;
            proofGraphs = new ArrayList<String>();

            for (var statement : node.statements()) {
                if (model.vocab().proof().equals(statement.predicate())) {
                    proofGraphs.add(statement.object());
                }
            }

        } else {
            proofGraphs = new ArrayList<String>();

            for (var node : graph.nodes().values()) {

                resource = node.id;
                proofGraphs.clear();

                for (var statement : node.statements()) {
                    if (model.vocab().proof().equals(statement.predicate())) {
                        proofGraphs.add(statement.object());
                    }
                }

                if (!proofGraphs.isEmpty()) {
                    break;
                }
                
                resource = null;
            }
        }
IO.println(">>> # " + resource + ", " + proofGraphs);
        return new GraphAccessor(
                model,
                null, // TODO
                null, // TODO
                null, // TODO
                resource,
                proofGraphs,
                dataset);
    }

    @Override
    public ProofCursor createProofCursor() {
        return model.createCursor(this);
    }

    @Override
    public SequencedCollection<?> context() {
        return context;
    }

    @Override
    public Map<String, ?> source() {
        return document;
    }

    @Override
    public Object document() {
        var graph = documentGraph();

        if (model != null) {
            // TODO cache

            if (resource != null) {
                var node = graph.nodes().get(resource);
                var mapper = model.documentMapper(node.type());
                if (mapper != null) {
                    return mapper.materialize(context, node, dataset, model);
                }
            }

            for (var node : graph.nodes().values()) {
                var mapper = model.documentMapper(node.type());
                if (mapper != null) {
                    resource = node.id();
                    return mapper.materialize(context, node, dataset, model);
                }
            }
        }

        return graph;
    }

    @Override
    public Graph documentGraph() {
        return dataset.get("@default");
    }

    @Override
    public Graph proofGraph(String graph) {
        return dataset.get(graph);
    }

    public Collection<String> proofGraphs() {
        return proofGraphs;
    }

    @Override
    public Map<String, ?> expandedData() {
        return expandedData;
    }

    private static class DatasetBuilder implements QuadConsumer {

        private final Map<String, Graph> graphs = new HashMap<>();

        private final Collection<String> proofGraphs = new HashSet<>();

        private String resource = null;

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
                    if (resource == null) {
                        resource = subject;

                    } else if (!resource.equals(subject)) {
                        throw new IllegalArgumentException("Multiple nodes with proof predicates ...");
                    }
                    proofGraphs.add(object);
                }
            }

            var container = graphs.computeIfAbsent(key, _ -> new Graph(graph, new LinkedHashMap<>()));

            var node = container.nodes().computeIfAbsent(
                    subject,
                    _ -> new Graph.Node(subject, container));

            node.addStatement(predicate, object, datatype, language, direction);
        }
    }

    @Override
    public Vocab vocab() {
        // FIXME read from JSON-LD term map after expansion
        return new Vocab("@context", "proof", "id", "type");
    }
}
