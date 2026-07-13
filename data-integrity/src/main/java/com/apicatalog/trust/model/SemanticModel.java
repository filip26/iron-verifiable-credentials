package com.apicatalog.trust.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.apicatalog.trust.processor.GraphProcessor;
import com.apicatalog.trust.processor.PayloadProcessor;
import com.apicatalog.trust.proof.GraphProofCursor;
import com.apicatalog.trust.proof.GraphProofReader;
import com.apicatalog.trust.proof.ProofCursor;

public class SemanticModel implements DataModel {

    // use just Supplier
    @FunctionalInterface
    @Deprecated
    public interface C14nFactory {
        GraphCanonizer newInstance();
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

    public interface GraphCanonizer {
        QuadConsumer consumer();

        byte[] canonize();

        void canonize(QuadConsumer consumer);

        Map<String, String> labels();

        String toNQuad(
                String subject,
                String predicate,
                String object,
                String datatype,
                String language,
                String direction,
                String graph);

        // TODO void reset();
    }

    private final GraphProcessor.Factory processorFactory;
    private final GraphProofCursor.Factory cursorFactory;
    private final Map<String, GraphProofReader> readers;

    private final String c14n;
    private final C14nFactory canonizeFactory;

    private final Function<Map<String, Object>, Collection<Object>> expand;
    private final BiFunction<Collection<String>, Map<String, Object>, Map<String, Object>> compact;
    private final BiConsumer<Object, QuadConsumer> tordf;

    public SemanticModel(
            GraphProcessor.Factory processorFactory,
            GraphProofCursor.Factory cursorFactory,
            String c14n,
            Function<Map<String, Object>, Collection<Object>> expand,
            BiFunction<Collection<String>, Map<String, Object>, Map<String, Object>> compact,
            BiConsumer<Object, QuadConsumer> tordf,
            C14nFactory canonizeFactory,
            Map<String, GraphProofReader> readers) {
        this.processorFactory = processorFactory;
        this.cursorFactory = cursorFactory;
        this.c14n = c14n;

        this.expand = expand;
        this.compact = compact;
        this.tordf = tordf;

        this.canonizeFactory = canonizeFactory;
        this.readers = readers;
    }

    @Override
    public String c14n() {
        return c14n;
    }

    @Override
    public PayloadProcessor createProcessor(Map<String, Object> document) {
        return processorFactory.newInstance(
                this,
                ModelResolver.getContexts(document),
                document);
    }
    
    @Override
    public ProofCursor createProofCursor(Collection<String> context, Map<String, Object> document) {

        var processor = processorFactory.newInstance(
                this,
                context,
                document);

        var proofs = processor.proofs();

        if (proofs == null || proofs.isEmpty()) {
            return null;
        }

        var proofReaders = new HashMap<String, GraphProofReader>(proofs.size());

        for (var proofGraph : proofs) {

            var proof = processor.proof(proofGraph);
            var proofType = processor.proofType(proofGraph);

            var reader = readers.get(proofType);

            if (reader != null && reader.isAccepted(proof)) {
                proofReaders.put(proofGraph, reader);
            }
        }

        if (proofReaders.isEmpty()) {
            return null;
        }

        return cursorFactory.newInstance(this, processor, proofReaders);
    }

    public GraphCanonizer newCanonizer() {
        return canonizeFactory.newInstance();
    }

    public BiConsumer<Object, QuadConsumer> tordf() {
        return tordf;
    }

    public Function<Map<String, Object>, Collection<Object>> expand() {
        return expand;
    }

    public BiFunction<Collection<String>, Map<String, Object>, Map<String, Object>> compact() {
        return compact;
    }
}
