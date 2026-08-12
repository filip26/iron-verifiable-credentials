package com.apicatalog.trust.semantic;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.apicatalog.trust.payload.PayloadGenerator;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.proof.ProofCursor;

public class GraphProofCursor implements ProofCursor {

    private final SemanticModel model;
    private final SemanticAccessor adapter;
    private final Map<String, Entry<String, GraphProofMapper>> readers;

    private Iterator<String> proofGraphsIterator;

    private Proof currentProof;
    private Graph currentProofGraph;
    private Entry<String, GraphProofMapper> currentReader;
    private PayloadGenerator payloadProvider;

    // TODO ?!?
    @FunctionalInterface
    public interface Factory {
        GraphProofCursor createCursor(
                SemanticModel model,
                SemanticAccessor processor);
    }

    protected GraphProofCursor(
            SemanticModel model,
            SemanticAccessor adapter,
            Map<String, Entry<String, GraphProofMapper>> readers) {
        this.model = model;
        this.adapter = adapter;
        this.readers = readers;

        this.proofGraphsIterator = adapter.proofGraphs().iterator();
        this.currentProof = null;
        this.currentProofGraph = null;
        this.currentReader = null;
        this.payloadProvider = model.createPayload(adapter);
    }

    public static GraphProofCursor newInstance(SemanticModel model, SemanticAccessor adapter) {

        var proofGraphs = adapter.proofGraphs();

        if (proofGraphs == null || proofGraphs.isEmpty()) {
            return null;
        }

        var proofReaders = HashMap.<String, Entry<String, GraphProofMapper>>newHashMap(proofGraphs.size());

        for (var proofGraphId : proofGraphs) {

            var proofGraph = adapter.proofGraph(proofGraphId);

            // find proof node and reader
            for (var node : proofGraph.nodes().values()) {

                // limit proof type to one
                if (node.type().size() != 1) {
                    throw new IllegalArgumentException(); // TODO
                }

                var proofType = node.type().getFirst();

                var reader = model.reader(proofType);

                if (reader != null && reader.accepts(node)) {
                    proofReaders.put(proofGraphId, Map.entry(node.id(), reader));
                    break;
                }
            }

//            var proofType = adapter.proofType(proofGraph);

//            var reader = model.reader(proofGraph.type().getFirst()); //TODO, only one type allowed
//
//            if (reader != null && reader.isAccepted(proofGraph)) {
//                proofReaders.put(proofGraphId, reader);
//            }
        }

        if (proofReaders.isEmpty()) {
            return null;
        }

        return new GraphProofCursor(model, adapter, proofReaders);
    }

    @Override
    public boolean isAccepted() {
        return currentReader != null && currentReader.getValue().accepts(currentProofGraph.nodes().get(currentReader.getKey()));
    }

    @Override
    public boolean next() {
        if (!proofGraphsIterator.hasNext()) {
            return false;
        }

        var proofGraph = proofGraphsIterator.next();

        currentProofGraph = adapter.proofGraph(proofGraph);
        currentReader = readers.get(proofGraph);
        currentProof = null;
        return true;
    }

    @Override
    public Proof proof() {
        if (currentProof == null && currentReader != null) {
            payloadProvider.reset();
            currentProof = currentReader.getValue().materialize(currentReader.getKey(), currentProofGraph, model, payloadProvider);
        }
        return currentProof;
    }
}
