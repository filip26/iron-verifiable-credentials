package com.apicatalog.trust.semantic;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import com.apicatalog.trust.payload.DigestiblePayload;
import com.apicatalog.trust.payload.GenericPayload;
import com.apicatalog.trust.payload.PayloadGenerator;

public class GraphPayloadGenerator implements PayloadGenerator {

    public interface Factory {
        PayloadGenerator createPayload(SemanticModel model, GraphAdapter processor);
    }

    protected final SemanticModel model;
    protected final GraphAdapter processor;

    protected Collection<String> includedProofs;

    public GraphPayloadGenerator(
            SemanticModel model,
            GraphAdapter processor) {
        this.model = model;
        this.processor = processor;
        this.includedProofs = null;
    }
    
    @Override
    public DigestiblePayload digestible() {
        return digestible(GenericPayload::new);
    }

    @Override
    public <T extends DigestiblePayload> T digestible(Function<byte[], T> payloadFactory) {

        var canonizer = model.newCanonizer();
        var consumer = canonizer.consumer();

        Set<String> selectedGraph = Set.of();

        if (includedProofs != null && !includedProofs.isEmpty()) {
            selectedGraph = new HashSet<String>();

            // select proofs

            for (var graph : processor.proofs()) {

                var proof = processor.proof(graph);

                if (includedProofs.contains(proof.iterator().next()[0])) {
                    selectedGraph.add(graph);
                    for (var quad : proof) {
                        consumer.accept(quad[0], quad[1], quad[2], quad[3], quad[4], quad[5], quad[6]);
                    }
                }
            }
        }

        for (var quad : processor.data()) {
            if (!model.vocab().proof().equals(quad[1])
                    || selectedGraph.contains(quad[2])) {
                consumer.accept(quad[0], quad[1], quad[2], quad[3], quad[4], quad[5], null);
            }
        }

        var canonical = canonizer.canonize();
        // TODO cache generic
        return payloadFactory.apply(canonical);
    }

    @Override
    public void withProofs(Collection<String> ids) {
        this.includedProofs = ids;
    }

    @Override
    public void reset() {
        this.includedProofs = null;
    }
}
