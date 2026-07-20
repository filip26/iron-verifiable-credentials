package com.apicatalog.trust.proof;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.apicatalog.trust.model.SemanticModel;
import com.apicatalog.trust.payload.PayloadGenerator;
import com.apicatalog.trust.processor.GraphProcessor;

public class GraphProofCursor implements ProofCursor {

    private final SemanticModel model;
    private final GraphProcessor processor;
    private final Map<String, GraphProofReader> readers;

    private Iterator<String> graphIterator;

    private Proof currentProof;
    private Collection<String[]> currentGraph;
    private GraphProofReader currentReader;
    private PayloadGenerator payloadProvider;

    // TODO ?!?
    @FunctionalInterface
    public interface Factory {
        GraphProofCursor createCursor(
                SemanticModel model,
                GraphProcessor processor);
    }

    protected GraphProofCursor(
            SemanticModel model,
            GraphProcessor processor,
            Map<String, GraphProofReader> readers) {
        this.model = model;
        this.processor = processor;
        this.readers = readers;

        this.graphIterator = processor.proofs().iterator();
        this.currentProof = null;
        this.currentGraph = null;
        this.currentReader = null;
        this.payloadProvider = model.createPayload(processor);
    }

    public static GraphProofCursor newInstance(SemanticModel model, GraphProcessor processor) {

        var proofs = processor.proofs();

        if (proofs == null || proofs.isEmpty()) {
            return null;
        }

        var proofReaders = new HashMap<String, GraphProofReader>(proofs.size());

        for (var proofGraph : proofs) {

            var proof = processor.proof(proofGraph);
            var proofType = processor.proofType(proofGraph);

            var reader = model.reader(proofType);

            if (reader != null && reader.isAccepted(proof)) {
                proofReaders.put(proofGraph, reader);
            }
        }

        if (proofReaders.isEmpty()) {
            return null;
        }

        return new GraphProofCursor(model, processor, proofReaders);
    }

    @Override
    public boolean isAccepted() {
        return currentReader != null && currentReader.isAccepted(currentGraph);
    }

    @Override
    public boolean next() {
        if (!graphIterator.hasNext()) {
            return false;
        }

        var graph = graphIterator.next();

        currentGraph = processor.proof(graph);
        currentReader = readers.get(graph);
        currentProof = null;
        return true;
    }

    @Override
    public Proof proof() {
        if (currentProof == null && currentReader != null) {
            payloadProvider.reset();
            currentProof = currentReader.read(currentGraph, model, payloadProvider);
        }
        return currentProof;
    }
}
