package com.apicatalog.trust.proof;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.apicatalog.trust.data.GraphData;
import com.apicatalog.trust.model.SemanticModel;
import com.apicatalog.trust.payload.DigestiblePayload;
import com.apicatalog.trust.payload.GenericPayload;
import com.apicatalog.trust.payload.PayloadSelector;
import com.apicatalog.trust.payload.RedactablePayload;

public class GraphProofCursor implements ProofCursor, PayloadSelector {

    private final SemanticModel model;

    private Map<String, Collection<String[]>> graphs;
    private Map<String, GraphProofReader> readers;

    private GraphData payload;
    private Iterator<Entry<String, Collection<String[]>>> iterator;

    private Proof currentProof;
    private Map.Entry<String, Collection<String[]>> currentEntry;
    private GraphProofReader currentProofReader;

    private Collection<String> includedProofs;

    @FunctionalInterface
    public interface Factory {
        GraphProofCursor newInstance(
                SemanticModel model,
                Map<String, Collection<String[]>> graphs,
                Map<String, GraphProofReader> readers);
    }

    public GraphProofCursor(
            SemanticModel model,
            Map<String, Collection<String[]>> graphs,
            Map<String, GraphProofReader> readers) {
        this.model = model;
        this.graphs = graphs;
        this.readers = readers;

        this.iterator = graphs.entrySet().stream().filter(entry -> !"@default".equals(entry.getKey())).iterator();
        this.currentProof = null;
        this.currentEntry = null;
        
        this.includedProofs = null;
    }

    @Override
    public GraphData data() {

        if (payload == null && graphs.containsKey("@default")) {

            var data = graphs.get("@default");

            payload = new GraphData(data, model.c14n());
        }

        return payload;
    }

    @Override
    public boolean isAccepted() {
        return currentProofReader != null && currentProofReader.isAccepted(currentEntry.getValue());
    }
    
    @Override
    public boolean next() {
        if (!iterator.hasNext()) {
            return false;
        }

        currentEntry = iterator.next();
        currentProof = null;
        currentProofReader = readers.get(currentEntry.getKey());
        return true;
    }

    @Override
    public Proof proof() {
        if (currentProof == null && currentProofReader != null) {
            currentProof = currentProofReader.read(currentEntry.getValue(), model, this);
        }
        return currentProof;
    }

    @Override
    public DigestiblePayload digestible() {
        
        var canonizer = model.newCanonizer();
        var consumer = canonizer.consumer();

        Set<String> selectedGraph = Set.of();

        if (includedProofs != null && !includedProofs.isEmpty()) {
            selectedGraph = new HashSet<String>();

            // select proofs
            for (var graph : graphs.entrySet()) {
                if ("@default".equals(graph.getKey())) {
                    continue;
                }
                if (includedProofs.contains(graph.getValue().iterator().next()[0])) {
                    selectedGraph.add(graph.getKey());
                    for (var quad : graph.getValue()) {
                        consumer.accept(quad[0], quad[1], quad[2], quad[3], quad[4], quad[5], quad[6]);
                    }
                }
            }
        }

        for (var quad : graphs.get("@default")) {
            if (!"https://w3id.org/security#proof".equals(quad[1])
                    || selectedGraph.contains(quad[2])) {
                consumer.accept(quad[0], quad[1], quad[2], quad[3], quad[4], quad[5], null);
            }
        }

        var canonical = canonizer.canonize();

        return new GenericPayload(canonical);        
    }

    @Override
    public void withProofs(Collection<String> ids) {
        this.includedProofs = ids;
    }

    @Override
    public RedactablePayload filter(Collection<String> pointers) {
        // TODO Auto-generated method stub
        return null;
    }
}
