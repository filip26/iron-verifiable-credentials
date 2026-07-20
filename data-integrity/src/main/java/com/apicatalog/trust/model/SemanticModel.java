package com.apicatalog.trust.model;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.apicatalog.di.std.GraphPayloadGenerator;
import com.apicatalog.trust.payload.PayloadGenerator;
import com.apicatalog.trust.processor.GraphProcessor;
import com.apicatalog.trust.proof.GraphProofCursor;
import com.apicatalog.trust.proof.GraphProofReader;

public class SemanticModel implements ProcessingModel {

    // "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"

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
    private final GraphPayloadGenerator.Factory payloadFactory;

    private final Map<String, GraphProofReader> readers;

    private final String c14n;
    private final Supplier<GraphCanonizer> canonizeFactory;

    private final Function<Map<String, Object>, Collection<Object>> expand;
    private final BiFunction<Collection<String>, Map<String, Object>, Map<String, Object>> compact;
    private final BiConsumer<Object, QuadConsumer> tordf;

    private final Vocab vocab;

    public SemanticModel(
            Vocab vocab,
            GraphProcessor.Factory processorFactory,
            GraphProofCursor.Factory cursorFactory,
            GraphPayloadGenerator.Factory payloadFactory,
            String c14n,
            Function<Map<String, Object>, Collection<Object>> expand,
            BiFunction<Collection<String>, Map<String, Object>, Map<String, Object>> compact,
            BiConsumer<Object, QuadConsumer> tordf,
            Supplier<GraphCanonizer> canonizeFactory,
            Map<String, GraphProofReader> readers) {
        this.vocab = vocab;
        this.processorFactory = processorFactory;
        this.cursorFactory = cursorFactory;
        this.payloadFactory = payloadFactory;
        this.c14n = c14n;

        this.expand = expand;
        this.compact = compact;
        this.tordf = tordf;

        this.canonizeFactory = canonizeFactory;
        this.readers = readers;
    }

    @Override
    public GraphProcessor createProcessor(Map<String, Object> document) {
        return processorFactory.createProcessor(
                this,
                ContextAwareResolver.getContexts(document),
                document);
    }

    public GraphProofCursor createCursor(GraphProcessor processor) {
        return cursorFactory.createCursor(this, processor);
    }

    public PayloadGenerator createPayload(GraphProcessor processor) {
        return payloadFactory.createPayload(this, processor);
    }

//    @Override
//    public ProofCursor createProofCursor(Collection<String> context, Map<String, Object> document) {
//
//        var processor = processorFactory.createProcessor(
//                this,
//                context,
//                document);
//
//        var proofs = processor.proofs();
//
//        if (proofs == null || proofs.isEmpty()) {
//            return null;
//        }
//
//        var proofReaders = new HashMap<String, GraphProofReader>(proofs.size());
//
//        for (var proofGraph : proofs) {
//
//            var proof = processor.proof(proofGraph);
//            var proofType = processor.proofType(proofGraph);
//
//            var reader = readers.get(proofType);
//
//            if (reader != null && reader.isAccepted(proof)) {
//                proofReaders.put(proofGraph, reader);
//            }
//        }
//
//        if (proofReaders.isEmpty()) {
//            return null;
//        }
//
//        return cursorFactory.newInstance(this, processor);
//    }

    public GraphProofReader reader(String proofType) {
        return readers.get(proofType);
    }

    public GraphCanonizer newCanonizer() {
        return canonizeFactory.get();
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

    @Override
    public Vocab vocab() {
        return vocab;
    }
}
