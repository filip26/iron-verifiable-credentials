package com.apicatalog.trust.proof;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.apicatalog.trust.data.Data;
import com.apicatalog.trust.data.GenericPayload;
import com.apicatalog.trust.data.GraphData;
import com.apicatalog.trust.model.GraphModel;
import com.apicatalog.trust.model.GraphModel.Canonizer;

/*
 * 1. JSON-LD expansion + type, uri map
 * 2. separating proof
 * 3. instantiate document function -> out
 * 4. instantiate proof(index) function -> out
 */
public class ProofGraphCursor implements ProofCursor {

    private final GraphModel model;

    Map<String, Collection<String[]>> graphs;
    Map<String, ProofGraphReader> readers;

    GraphData payload;
    Iterator<Entry<String, Collection<String[]>>> iterator;

    Proof currentProof;
    Map.Entry<String, Collection<String[]>> currentEntry;
    ProofGraphReader currentProofReader;

    @FunctionalInterface
    public interface Factory {
        ProofGraphCursor newInstance(
                GraphModel model,
                Map<String, Collection<String[]>> graphs,
                Map<String, ProofGraphReader> readers);
    }

    public ProofGraphCursor(
            GraphModel model,
            Map<String, Collection<String[]>> graphs,
            Map<String, ProofGraphReader> readers) {
        this.model = model;
        this.graphs = graphs;
        this.readers = readers;

        this.iterator = graphs.entrySet().stream().filter(entry -> !"@default".equals(entry.getKey())).iterator();
        this.currentProof = null;
        this.currentEntry = null;
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
            currentProof = currentProofReader.read(currentEntry.getValue(), this);
        }
        return currentProof;
    }

    public Data data(Collection<String> previous) {
        var data = data();

        if (data.digestiblePayload(previous) == null) {

            var canonizer = model.newCanonizer();
            var consumer = canonizer.consumer();

            Set<String> selectedGraph = Set.of();

            if (previous != null && !previous.isEmpty()) {
                selectedGraph = new HashSet<String>();

                // select proofs
                for (var graph : graphs.entrySet()) {
                    if ("@default".equals(graph.getKey())) {
                        continue;
                    }
                    if (previous.contains(graph.getValue().iterator().next()[0])) {
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

            data.digestiblePayload(previous, new GenericPayload(canonical));
        }
        return data;
    }

    public Canonizer newCanonizer() {
        return model.newCanonizer();
    }
}
