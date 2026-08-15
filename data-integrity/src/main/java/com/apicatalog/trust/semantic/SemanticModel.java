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

    public interface GraphCanonizer extends QuadConsumer {

//        QuadConsumer consumer();

        byte[] canonize();

//        void canonize(QuadConsumer consumer);
//
//        Map<String, String> labels();
//
//        String toNQuad(
//                String subject,
//                String predicate,
//                String object,
//                String datatype,
//                String language,
//                String direction,
//                String graph);

        // TODO void reset();
    }

    public record Primitives(
            SemanticModel.Accessor.Factory adapter,
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

    private final Map<String, GraphProofMapper> readers;

    public SemanticModel(
            Vocab vocab,
            Primitives primitives,
            JsonLdOps jsonLd,
            Supplier<GraphCanonizer> canonizeFactory,
            Map<String, GraphProofMapper> readers) {
        this.vocab = vocab;
        this.primitives = primitives;
        this.jsonLd = jsonLd;

        this.canonizeFactory = canonizeFactory;
        this.readers = readers;
    }

    @Override
    public SemanticModel.Accessor createAccessor(Map<String, Object> document) {
        return primitives.adapter.createAdapter(
                this,
                ContextAwareResolver.getContexts(document),
                document);
    }

    @Override
    public Document.Updater createUpdater(Map<String, Object> document) {
        return primitives.updater.createUpdater(this, createAccessor(document));
    }

    public PayloadGenerator createPayload(SemanticModel.Accessor adapter) {
        return primitives.payload.createPayload(this, adapter);
    }

    public GraphProofCursor createCursor(SemanticModel.Accessor adapter) {
        return primitives.cursor.createCursor(this, adapter);
    }

    public GraphProofMapper reader(String type) {
        return readers.get(type);
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
    
    public interface Accessor extends Document.Accessor {

        @FunctionalInterface
        public interface Factory {
            SemanticModel.Accessor createAdapter(
                    SemanticModel model,
                    Collection<String> context,
                    Map<String, Object> document);
        }

        Collection<String> context();

        Graph data();

        // returns proof graph ids, might be URI or blank node identifier
        Collection<String> proofGraphs();

        Graph proofGraph(String graph);

        Map<String, Object> expandedData();

        Vocab vocab();

        Map<String, ?> source();
    }
}
