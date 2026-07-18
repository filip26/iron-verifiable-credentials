package com.apicatalog.di.std;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.apicatalog.trust.model.SemanticModel;
import com.apicatalog.trust.model.SemanticModel.QuadConsumer;
import com.apicatalog.trust.payload.PayloadGenerator;
import com.apicatalog.trust.processor.GraphProcessor;
import com.apicatalog.trust.proof.GraphProofCursor;
import com.apicatalog.trust.proof.ProofCursor;

public class StandardGraphProcessor implements GraphProcessor {

    private final SemanticModel model;
    private final Collection<String> context;
    private final Map<String, Object> document;

    private Dataset dataset;

    public StandardGraphProcessor(
            SemanticModel model,
            Collection<String> context,
            Map<String, Object> document) {
        this.model = model;
        this.context = context;
        this.document = document;
    }

    @Override
    public ProofCursor createProofCursor() {
        return model.createCursor(this);
    }

    @Override
    public PayloadGenerator createPayload() {
        return new GraphPayloadGenerator(model, this);
    }

    @Override
    public Collection<String> context() {
        return context;
    }

    @Override
    public Collection<String[]> data() {
        lazyInit();
        return dataset.graphs.get("@default");
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
