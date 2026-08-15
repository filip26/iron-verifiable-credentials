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
        PayloadGenerator createPayload(SemanticModel model, SemanticAccessor processor);
    }

    protected final SemanticModel model;
    protected final SemanticAccessor adapter;

    protected Collection<String> includedProofs;
    protected byte[] genericPayload;

    public GraphPayloadGenerator(
            SemanticModel model,
            SemanticAccessor adapter) {
        this.model = model;
        this.adapter = adapter;
        this.includedProofs = List.of();
        this.genericPayload = null;
    }

    @Override
    public DigestiblePayload digestible() {
        return digestible(GenericPayload::new);
    }

    @Override
    public <T extends DigestiblePayload> T digestible(Function<byte[], T> payloadFactory) {

        if (includedProofs.isEmpty() && genericPayload != null) {
            return payloadFactory.apply(genericPayload);
        }

        var canonizer = model.newCanonizer();

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
                        for (var statement : proofNode.statements()) {
                            canonizer.accept(
                                    proofNode.id(),
                                    statement.predicate(),
                                    statement.object(),
                                    statement.datatype(),
                                    statement.language(),
                                    statement.direction(),
                                    proofNode.graph());
                        }
                    }
                }
            }
        }

        for (var node : adapter.data().nodes().values()) {
            for (var statement : node.statements()) {
                // do not include proof predicates if not selected
                if (!model.vocab().proof().equals(statement.predicate())
                        || selectedGraph.contains(statement.object())) {
                    canonizer.accept(
                            node.id(),
                            statement.predicate(),
                            statement.object(),
                            statement.datatype(),
                            statement.language(),
                            statement.direction(),
                            node.graph());
                }
            }
        }

        var canonical = canonizer.canonize();
        
        if (includedProofs.isEmpty() && genericPayload == null) {
            genericPayload = canonical;
        }
        
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
