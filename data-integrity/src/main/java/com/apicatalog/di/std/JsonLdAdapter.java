package com.apicatalog.di.std;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import com.apicatalog.trust.model.ProcessingModel.Vocab;
import com.apicatalog.trust.proof.ProofCursor;
import com.apicatalog.trust.semantic.GraphAdapter;
import com.apicatalog.trust.semantic.SemanticModel;
import com.apicatalog.trust.semantic.SemanticModel.QuadConsumer;

public final class JsonLdAdapter implements GraphAdapter {

    private final SemanticModel model;

    private final Collection<String> context;
    private final Map<String, Object> document;

    private Map<String, Object> expandedData;
    private Collection<?> expandedProofs;

    private Dataset dataset;

    protected JsonLdAdapter(
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

    public static JsonLdAdapter newInstance(
            SemanticModel model,
            Collection<String> context,
            Map<String, Object> document) {
        return new JsonLdAdapter(model, context, document);
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

        if (expanded.iterator().next() instanceof Map map) {
            expandedData = new LinkedHashMap<String, Object>(map);
            if (map.containsKey(model.vocab().proof())) {
                var proofs = expandedData.remove(model.vocab().proof());
                if (proofs instanceof Collection<?> col) {
                    expandedProofs = col;
                } else {
                    throw new IllegalArgumentException();
                }
            }

        } else {
            throw new IllegalArgumentException();
        }

//        if (expandedProofs != null) {
        dataset = new Dataset();
        dataset.proofPredicate = model.vocab().proof();
        dataset.typePredicate = model.vocab().type();
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

        private final Map<String, String> proofTypes = new HashMap<>();

        private Map<String, Collection<String[]>> graphs = new HashMap<>();

        private Collection<String> proofGraphs = new HashSet<>();

        private String proofPredicate;
        private String typePredicate;

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

                if (proofPredicate.equals(predicate)) {
                    proofGraphs.add(object);
                }

            } else if (typePredicate.equals(predicate)) {
                proofTypes.put(graph, object);
            }

            graphs.computeIfAbsent(key, (_) -> new ArrayList<String[]>())
                    .add(new String[] {
                            subject, predicate, object, datatype, language, direction, graph
                    });
        }
    }

    @Override
    public Vocab keys() {
        // FIXME read from JSON-LD term map after expansion
        return new Vocab("@context", "proof", "id", "type");
    }

    @Override
    public Map<String, ?> compacted() {
        return document;
    }
}
