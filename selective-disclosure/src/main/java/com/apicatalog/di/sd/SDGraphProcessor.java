package com.apicatalog.di.sd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.apicatalog.trust.model.SemanticModel;
import com.apicatalog.trust.model.SemanticModel.QuadConsumer;
import com.apicatalog.trust.processor.GraphProcessor;
import com.apicatalog.trust.proof.ProofCursor;

public class SDGraphProcessor implements GraphProcessor {

    private final SemanticModel model;
    private final Collection<String> context;
    private final Map<String, Object> document;

    private Map<String, Object> expandedDocument;
    private Dataset dataset;

    public SDGraphProcessor(
            SemanticModel model,
            Collection<String> context,
            Map<String, Object> document) {
        this.model = model;
        this.context = context;
        this.document = document;

        this.expandedDocument = null;
        this.dataset = null;
    }

    @Override
    public Collection<String> context() {
        return context;
    }

    @Override
    public Collection<String[]> proof(String graph) {
        lazyInit();
        return dataset.graphs.get(graph);
    }

    @Override
    public String proofType(String graph) {
        lazyInit();
        return dataset.proofTypes.get(graph);
    }

    @Override
    public Collection<String> proofs() {
        lazyInit();
        return dataset.proofGraphs;
    }

    public Map<String, Object> expandedDocument() {
        lazyInit();
        return expandedDocument;
    };

    private void lazyInit() {

        if (expandedDocument != null || dataset != null) {
            return;
        }

        var expanded = model.expand().apply(document);

        if (expanded.size() != 1) {
            throw new IllegalArgumentException();
        }

        Object expandedProofs = null;

        if (expanded.iterator().next() instanceof Map map) {
            expandedDocument = new HashMap<String, Object>(map);
            if (map.containsKey("https://w3id.org/security#proof")) {
                expandedProofs = Map.of("https://w3id.org/security#proof",
                        expandedDocument.remove("https://w3id.org/security#proof"));
            }

        } else {
            throw new IllegalArgumentException();
        }

        if (expandedProofs != null) {
            dataset = new Dataset();
            model.tordf().accept(expandedProofs, dataset);
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

    public static record SignatureAlgorithm(String signature, String digest) {
    }

    @Override
    public ProofCursor createProofCursor() {
        return model.createCursor(this);
    }

    @Override
    public Collection<String[]> data() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SDPayloadGenerator createPayload() {
        return new SDPayloadGenerator(model, this);
    }
}
