package com.apicatalog.trust.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.apicatalog.trust.proof.ProofCursor;
import com.apicatalog.trust.proof.ProofGraphCursor.Factory;
import com.apicatalog.trust.proof.ProofGraphReader;

public class GraphModel implements Model {

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
    private final Collection<ProofGraphReader> readers;

    public GraphModel(
            Factory factory,
            String c14n,
            BiConsumer<Map<String, Object>, QuadConsumer> tordf,
            C14nFactory canonizeFactory,
            Collection<ProofGraphReader> readers) {
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

        var graphBuilder = new GraphBuilder();

        tordf.accept(document, graphBuilder);

        var graphs = graphBuilder.get();

//        var canonized = rdfc.canonize();

//        IO.println(canonized);

        // identify proofs
        var proofGraphs = graphs.get("@default").stream()
                .filter(statement -> "https://w3id.org/security#proof".equals(statement[1]))
                .map(statement -> statement[2]).toList();

//        IO.println(proofGraphs);

        if (proofGraphs.isEmpty()) {
            return null;
        }

        var graphReaders = new HashMap<String, ProofGraphReader>(proofGraphs.size());

        for (var proofGraph : proofGraphs) {

            var proof = graphs.get(proofGraph);

            for (var proofReader : readers) {
                if (proofReader.isAccepted(proof)) {
                    graphReaders.put(proofGraph, proofReader);
                    break;
                }
            }
        }

        if (graphReaders.isEmpty()) {
            return null;
        }

        return cursorFactory.newInstance(this, graphs, graphReaders);
    }

    private static class GraphBuilder implements QuadConsumer {

        private Map<String, Collection<String[]>> graphMap = new HashMap<>();

        public Map<String, Collection<String[]>> get() {
            return graphMap;
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
            }

            graphMap.computeIfAbsent(key, (_) -> new ArrayList<String[]>())
                    .add(new String[] {
                            subject, predicate, object, datatype, language, direction, graph
                    });
        }

    }

    public Canonizer newCanonizer() {
        return canonizeFactory.newInstance();
    }
}
