package com.apicatalog.trust.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.BiConsumer;

import com.apicatalog.trust.proof.ProofCursor;
import com.apicatalog.trust.proof.GraphProofCursor.Factory;
import com.apicatalog.trust.proof.GraphProofReader;

public class SematicModel implements Model {

    @FunctionalInterface
    public interface C14nFactory {
        Canonizer newInstance();
    }

    @FunctionalInterface
    public interface QuadConsumer {
        void accept(
                String subject,
                String predicate,
                String object,
                String datatype,
                String language,
                String direction,
                String graph);
    }

    public interface Canonizer {
        QuadConsumer consumer();

        byte[] canonize();
    }

    // Map<String, Collection<String[]

    private final Factory cursorFactory;
    private final String c14n;
    private final BiConsumer<Map<String, Object>, QuadConsumer> tordf;
    private final C14nFactory canonizeFactory;
    private final Map<String, GraphProofReader> readers;

    public SematicModel(
            Factory factory,
            String c14n,
            BiConsumer<Map<String, Object>, QuadConsumer> tordf,
            C14nFactory canonizeFactory,
            Map<String, GraphProofReader> readers) {
        this.cursorFactory = factory;
        this.c14n = c14n;
        this.tordf = tordf;
        this.canonizeFactory = canonizeFactory;
        this.readers = readers;
    }

    @Override
    public String c14n() {
        return c14n;
    }

    @Override
    public ProofCursor createCursor(Collection<String> context, Map<String, Object> document) {

        var dataset = new DatasetProvider(readers);

        tordf.accept(document, dataset);

        if (dataset.noProofsFound() || dataset.noProofReaders()) {
            return null;
        }

        var graphs = dataset.graphs();

        var proofReaders = new HashMap<String, GraphProofReader>(dataset.mapping);

        for (var entry : dataset.mapping.entrySet()) {

            var proof = graphs.get(entry.getKey());

            var reader = entry.getValue();
            if (reader.isAccepted(proof)) {
                proofReaders.put(entry.getKey(), reader);
            }
        }

        if (proofReaders.isEmpty()) {
            return null;
        }

        return cursorFactory.newInstance(this, graphs, proofReaders);
    }

    private static class DatasetProvider implements QuadConsumer {

        private final Map<String, GraphProofReader> readers;

        private Map<String, Collection<String[]>> dataset = new HashMap<>();

        private Collection<String> proofGraphs = new HashSet<>();

        private Map<String, GraphProofReader> mapping = new HashMap<>();

        public DatasetProvider(Map<String, GraphProofReader> readers) {
            this.readers = readers;
        }

        public boolean noProofReaders() {
            return mapping.isEmpty();
        }

        public Map<String, Collection<String[]>> graphs() {
            return dataset;
        }

        public boolean noProofsFound() {
            return proofGraphs.isEmpty();
        }

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
                var reader = readers.get(object);
                if (reader != null) {
                    mapping.put(graph, reader);
                }
            }

//            IO.println("X " + Arrays.toString(new String[] {
//                    subject, predicate, object, datatype, language, direction, graph
//            }));

//var proofGraphs = graphs.get("@default").stream()
//.filter(statement -> "https://w3id.org/security#proof".equals(statement[1]))
//.map(statement -> statement[2]).toList();

            dataset.computeIfAbsent(key, (_) -> new ArrayList<String[]>())
                    .add(new String[] {
                            subject, predicate, object, datatype, language, direction, graph
                    });
        }

    }

    public Canonizer newCanonizer() {
        return canonizeFactory.newInstance();
    }
}
