package com.apicatalog.trust.semantic;

import java.util.Collection;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.apicatalog.trust.Document;
import com.apicatalog.trust.model.Model;
import com.apicatalog.trust.payload.PayloadGenerator;
import com.apicatalog.trust.semantic.Graph.NodeMapper;

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
            Function<Map<String, ?>, SequencedCollection<?>> expand,
            BiFunction<Collection<?>, Map<String, ?>, Map<String, Object>> compact,
            BiConsumer<Object, QuadConsumer> tordf) {
    };

    private final Vocab vocab;

    private final Primitives primitives;

    private final JsonLdOps jsonLd;

    private final Supplier<GraphCanonizer> canonizeFactory;

    private final Function<Collection<String>, NodeMapper<?>> documentMapper;
    private final Map<String, GraphProofMapper> proofMappers;

    public SemanticModel(
            Vocab vocab,
            Primitives primitives,
            JsonLdOps jsonLd,
            Supplier<GraphCanonizer> canonizeFactory,
            Function<Collection<String>, NodeMapper<?>> documentMapper, 
            Map<String, GraphProofMapper> proofMappers) {
        this.vocab = vocab;
        this.primitives = primitives;
        this.jsonLd = jsonLd;

        this.canonizeFactory = canonizeFactory;
        
        this.documentMapper = documentMapper;
        this.proofMappers = proofMappers;
    }

    @Override
    public SemanticModel.Accessor createAccessor(Collection<?> context, Map<String, ?> document) {
        return primitives.adapter.createAdapter(
                this,
                context,
                document);
    }

    @Override
    public Document.Updater createUpdater(Collection<?> context, Map<String, ?> document) {
        return primitives.updater.createUpdater(this, createAccessor(context, document));
    }

    public PayloadGenerator createPayload(SemanticModel.Accessor adapter) {
        return primitives.payload.createPayload(this, adapter);
    }

    public GraphProofCursor createCursor(SemanticModel.Accessor adapter) {
        return primitives.cursor.createCursor(this, adapter);
    }

    public NodeMapper<?> documentMapper(Collection<String> types) {
        return documentMapper.apply(types);
    }
    
    public GraphProofMapper proofMapper(String type) {
        return proofMappers.get(type);
    }

    public GraphCanonizer newCanonizer() {
        return canonizeFactory.get();
    }

    public BiConsumer<Object, QuadConsumer> tordf() {
        return jsonLd.tordf;
    }

    public Function<Map<String, ?>, SequencedCollection<?>> expand() {
        return jsonLd.expand;
    }

    public BiFunction<Collection<?>, Map<String, ?>, Map<String, Object>> compact() {
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
                    Collection<?> context,
                    Map<String, ?> document);
        }

        Collection<?> context();

        @Override
        Graph document();

        // returns proof graph ids, might be URI or blank node identifier
        Collection<String> proofGraphs();

        Graph proofGraph(String graph);

        Map<String, Object> expandedData();

        Vocab vocab();

        Map<String, ?> source();
    }
}
