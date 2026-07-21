package com.apicatalog.trust.semantic;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.apicatalog.trust.Document;
import com.apicatalog.trust.model.ContextAwareResolver;
import com.apicatalog.trust.model.Model;
import com.apicatalog.trust.payload.PayloadGenerator;

public class SemanticModel implements Model {

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

    private final SemanticAdapter.Factory processorFactory;
    private final GraphProofCursor.Factory cursorFactory;
    private final GraphPayloadGenerator.Factory payloadFactory;

    private final Map<String, GraphProofReader> readers;

    private final Supplier<GraphCanonizer> canonizeFactory;

    private final Function<Map<String, Object>, Collection<Object>> expand;
    private final BiFunction<Collection<String>, Map<String, Object>, Map<String, Object>> compact;
    private final BiConsumer<Object, QuadConsumer> tordf;

    private final Vocab vocab;

    public SemanticModel(
            Vocab vocab,
            SemanticAdapter.Factory processorFactory,
            GraphProofCursor.Factory cursorFactory,
            GraphPayloadGenerator.Factory payloadFactory,
            Function<Map<String, Object>, Collection<Object>> expand,
            BiFunction<Collection<String>, Map<String, Object>, Map<String, Object>> compact,
            BiConsumer<Object, QuadConsumer> tordf,
            Supplier<GraphCanonizer> canonizeFactory,
            Map<String, GraphProofReader> readers) {
        this.vocab = vocab;
        this.processorFactory = processorFactory;
        this.cursorFactory = cursorFactory;
        this.payloadFactory = payloadFactory;

        this.expand = expand;
        this.compact = compact;
        this.tordf = tordf;

        this.canonizeFactory = canonizeFactory;
        this.readers = readers;
    }

    @Override
    public SemanticAdapter createAdapter(Map<String, Object> document) {
        return processorFactory.createProcessor(
                this,
                ContextAwareResolver.getContexts(document),
                document);
    }

    @Override
    public Document.Updater createUpdater(Map<String, Object> document) {
        return new GraphUpdater(this, createAdapter(document));
    }

    public PayloadGenerator createPayload(SemanticAdapter adapter) {
        return payloadFactory.createPayload(this, adapter);
    }

    public GraphProofCursor createCursor(SemanticAdapter adapter) {
        return cursorFactory.createCursor(this, adapter);
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
