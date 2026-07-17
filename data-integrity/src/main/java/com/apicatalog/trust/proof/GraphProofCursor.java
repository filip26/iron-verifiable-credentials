package com.apicatalog.trust.proof;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.apicatalog.trust.document.GraphData;
import com.apicatalog.trust.model.SemanticModel;
import com.apicatalog.trust.processor.GraphProcessor;

public class GraphProofCursor implements ProofCursor {

    private final SemanticModel model;
    private final GraphProcessor processor;

//    private Map<String, Collection<String[]>> graphs;
    private Map<String, GraphProofReader> readers;

    private GraphData document;
    private Iterator<String> iterator;

    private Proof currentProof;
    private Collection<String[]> currentGraphProof;
    private GraphProofReader currentProofReader;

    @FunctionalInterface
    public interface Factory {
        GraphProofCursor newInstance(
                SemanticModel model,
                GraphProcessor processor,
                Map<String, GraphProofReader> readers);
    }

    public GraphProofCursor(
            SemanticModel model,
            GraphProcessor processor,
            Map<String, GraphProofReader> readers) {
        this.model = model;
        this.processor = processor;
        this.readers = readers;

        this.iterator = processor.proofs().iterator();
        this.currentProof = null;
        this.currentGraphProof = null;
        this.currentProofReader = null;
    }

    @Override
    public boolean isAccepted() {
        return currentProofReader != null && currentProofReader.isAccepted(currentGraphProof);
    }

    @Override
    public boolean next() {
        if (!iterator.hasNext()) {
            return false;
        }

        var graph = iterator.next();
        
        currentGraphProof = processor.proof(graph);
        currentProof = null;
        currentProofReader = readers.get(graph);
        processor.reset();
        return true;
    }

    @Override
    public Proof proof() {
        if (currentProof == null && currentProofReader != null) {
            currentProof = currentProofReader.read(currentGraphProof, model, processor);
        }
        return currentProof;
    }
}
