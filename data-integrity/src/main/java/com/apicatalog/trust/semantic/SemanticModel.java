package com.apicatalog.trust.semantic;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.apicatalog.trust.model.ContextAwareResolver;
import com.apicatalog.trust.model.ProcessingModel;
import com.apicatalog.trust.model.ProcessingModel.Vocab;
import com.apicatalog.trust.payload.PayloadGenerator;

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

    private final GraphAdapter.Factory processorFactory;
    private final GraphProofCursor.Factory cursorFactory;
    private final GraphPayloadGenerator.Factory payloadFactory;

    private final Map<String, GraphProofReader> readers;
//    private final Map<String, GraphProofWriter> writers;

    private final String c14n;
    private final Supplier<GraphCanonizer> canonizeFactory;

    private final Function<Map<String, Object>, Collection<Object>> expand;
    private final BiFunction<Collection<String>, Map<String, Object>, Map<String, Object>> compact;
    private final BiConsumer<Object, QuadConsumer> tordf;

    private final Vocab vocab;

    public SemanticModel(
            Vocab vocab,
            GraphAdapter.Factory processorFactory,
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
    public GraphUpdater createUpdater(Map<String, Object> document) {
        return null;
    }
    
    @Override
    public GraphAdapter createAdapter(Map<String, Object> document) {
        return processorFactory.createProcessor(
                this,
                ContextAwareResolver.getContexts(document),
                document);
    }

    public GraphProofCursor createCursor(GraphAdapter processor) {
        return cursorFactory.createCursor(this, processor);
    }

    public PayloadGenerator createPayload(Map<String, Object> document) {
        return createPayload(createAdapter(document));
    }
    
    public PayloadGenerator createPayload(GraphAdapter processor) {
        return payloadFactory.createPayload(this, processor);
    }

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
