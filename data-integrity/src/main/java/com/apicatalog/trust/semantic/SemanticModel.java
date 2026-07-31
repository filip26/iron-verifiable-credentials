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

    public record Primitives(
            SemanticAdapter.Factory adapter,
            GraphUpdater.Factory updater,
            GraphProofCursor.Factory cursor,
            GraphPayloadGenerator.Factory payload) {
    };

    public record JsonLdOps(
            Function<Map<String, Object>, Collection<Object>> expand,
            BiFunction<Collection<String>, Map<String, Object>, Map<String, Object>> compact,
            BiConsumer<Object, QuadConsumer> tordf) {
    };

    private final Vocab vocab;

    private final Primitives primitives;

    private final JsonLdOps jsonLd;

    private final Supplier<GraphCanonizer> canonizeFactory;

    private final Map<String, GraphProofReader> readers;

    public SemanticModel(
            Vocab vocab,
            Primitives primitives,
            JsonLdOps jsonLd,
            Supplier<GraphCanonizer> canonizeFactory,
            Map<String, GraphProofReader> readers) {
        this.vocab = vocab;
        this.primitives = primitives;
        this.jsonLd = jsonLd;

        this.canonizeFactory = canonizeFactory;
        this.readers = readers;
    }

    @Override
    public SemanticAdapter createAdapter(Map<String, Object> document) {
        return primitives.adapter.createAdapter(
                this,
                ContextAwareResolver.getContexts(document),
                document);
    }

    @Override
    public Document.Updater createUpdater(Map<String, Object> document) {
        return primitives.updater.createUpdater(this, createAdapter(document));
    }

    public PayloadGenerator createPayload(SemanticAdapter adapter) {
        return primitives.payload.createPayload(this, adapter);
    }

    public GraphProofCursor createCursor(SemanticAdapter adapter) {
        return primitives.cursor.createCursor(this, adapter);
    }

    public GraphProofReader reader(String proofType) {
        return readers.get(proofType);
    }

    public GraphCanonizer newCanonizer() {
        return canonizeFactory.get();
    }

    public BiConsumer<Object, QuadConsumer> tordf() {
        return jsonLd.tordf;
    }

    public Function<Map<String, Object>, Collection<Object>> expand() {
        return jsonLd.expand;
    }

    public BiFunction<Collection<String>, Map<String, Object>, Map<String, Object>> compact() {
        return jsonLd.compact;
    }

    @Override
    public Vocab vocab() {
        return vocab;
    }
}
