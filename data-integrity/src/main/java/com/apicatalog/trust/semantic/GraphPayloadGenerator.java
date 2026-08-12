package com.apicatalog.trust.semantic;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import com.apicatalog.trust.payload.DigestiblePayload;
import com.apicatalog.trust.payload.GenericPayload;
import com.apicatalog.trust.payload.PayloadGenerator;

public class GraphPayloadGenerator implements PayloadGenerator {

    @FunctionalInterface
    public interface Factory {
        PayloadGenerator createPayload(SemanticModel model, SemanticAdapter processor);
    }

    protected final SemanticModel model;
    protected final SemanticAdapter adapter;

    protected Collection<String> includedProofs;

    public GraphPayloadGenerator(
            SemanticModel model,
            SemanticAdapter adapter) {
        this.model = model;
        this.adapter = adapter;
        this.includedProofs = List.of();
    }

    @Override
    public DigestiblePayload digestible() {
        return digestible(GenericPayload::new);
    }

    @Override
    public <T extends DigestiblePayload> T digestible(Function<byte[], T> payloadFactory) {

        var canonizer = model.newCanonizer();
//        var consumer = canonizer.consumer();

        Set<String> selectedGraph = Set.of();

        if (!includedProofs.isEmpty()) {
            selectedGraph = HashSet.<String>newHashSet(includedProofs.size());

            // select proofs
            for (var proofGraphId : adapter.proofGraphs()) {

                var proofGraph = adapter.proofGraph(proofGraphId);

                var selected = false;

                for (var included : includedProofs) {
                    if (proofGraph.nodes().containsKey(included)) {
                        selected = true;
                        break;
                    }
                }

                if (selected) {

                    selectedGraph.add(proofGraphId);

                    for (var proofNode : proofGraph.nodes().values()) {
                        for (var quad : proofNode.statements()) {
                            canonizer.accept(proofNode.id(), quad[0], quad[1], quad[2], quad[3], quad[4], quad[5]);
                        }
                    }
                }
            }

//            for (var includedProofId : includedProofs) {
//
//                var proofNode = adapter.proof(includedProofId);
//
//                if (proofNode == null) {
//                    throw new IllegalArgumentException();
//                }
//
//                for (var quad : proofNode.statements()) {
//                    canonizer.accept(proofNode.id(), quad[0], quad[1], quad[2], quad[3], quad[4], quad[5]);
//                }
//
//                // FIXME
            //// if (includedProofs.contains(proofNode.iterator().next()[0])) { /
            /// selectedGraph.add(graph); / for (var quad : proofNode) { /
            /// consumer.accept(quad[0], quad[1], quad[2], quad[3], quad[4], quad[5],
            /// quad[6]); / } / }
//            }
        }

        for (var node : adapter.data().nodes().values()) {
            for (var quad : node.statements()) {
                // do not include proof predicates if not selected
                if (!model.vocab().proof().equals(quad[0]) || selectedGraph.contains(quad[1])) {
                    canonizer.accept(node.id(), quad[0], quad[1], quad[2], quad[3], quad[4], quad[5]);
                }
            }
        }

        var canonical = canonizer.canonize();
        // TODO cache generic, i.e. no included proofs
        return payloadFactory.apply(canonical);
    }

    @Override
    public void withProofs(Collection<String> ids) {
        Objects.requireNonNull(ids);
        this.includedProofs = ids;
    }

    @Override
    public void reset() {
        this.includedProofs = List.of();
    }
}
