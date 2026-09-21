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
    private final SemanticModel.Accessor adapter;
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
                SemanticModel.Accessor processor);
    }

    protected GraphProofCursor(
            SemanticModel model,
            SemanticModel.Accessor adapter,
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

    public static GraphProofCursor newInstance(SemanticModel model, SemanticModel.Accessor adapter) {

        var proofGraphs = adapter.proofGraphs();

        if (proofGraphs == null || proofGraphs.isEmpty()) {
            return null;
        }

        var proofMappers = HashMap.<String, Entry<String, GraphProofMapper>>newHashMap(proofGraphs.size());

        for (var proofGraphId : proofGraphs) {

            var proofGraph = adapter.proofGraph(proofGraphId);

            // find proof node and reader
            for (var node : proofGraph.nodes().values()) {

                // limit proof type to one
                if (node.type().size() != 1) {
                    throw new IllegalArgumentException(); // TODO
                }

                var proofType = node.type().getFirst();

                var mapper = model.proofMapper(proofType);

                if (mapper != null && mapper.accepts(node)) {
                    proofMappers.put(proofGraphId, Map.entry(node.id(), mapper));
                    break;
                }
            }
        }

//        if (proofReaders.isEmpty()) {
        //// return null;
//        }

        return new GraphProofCursor(model, adapter, proofMappers);
    }

    @Override
    public boolean isAccepted() {
        return currentReader != null
                && currentReader.getValue().accepts(currentProofGraph.nodes().get(currentReader.getKey()));
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
            currentProof = currentReader.getValue().materialize(currentReader.getKey(), currentProofGraph, model,
                    payloadProvider);
        }
        return currentProof;
    }

    public Graph proofGraph() {
        return currentProofGraph;
    }

    @Override
    public String proofType() {
        return currentReader != null && currentProofGraph != null
                ? currentProofGraph.nodes().get(currentReader.getKey()).type().getFirst()
                : null;
    }
}
