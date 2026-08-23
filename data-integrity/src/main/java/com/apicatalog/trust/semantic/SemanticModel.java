package com.apicatalog.trust.semantic;

import java.util.Collection;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.apicatalog.trust.Document;
import com.apicatalog.trust.model.Model;
import com.apicatalog.trust.payload.PayloadGenerator;
import com.apicatalog.trust.semantic.Graph.NodeMapper;
import com.apicatalog.trust.semantic.Graph.TypeMapping;

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
            BiFunction<Collection<?>, Map<String, ?>, Map<String, ?>> compact,
            BiConsumer<Object, QuadConsumer> tordf) {
    };

    private final Vocab vocab;

    private final Primitives primitives;

    private final JsonLdOps jsonLd;

    private final Supplier<GraphCanonizer> canonizeFactory;

    private final Graph.TypeMappingMatcher typeMatcher;
    @Deprecated
    private final Function<Collection<String>, NodeMapper<?>> documentMapper;
    private final Map<String, GraphProofMapper> proofMappers;

    public SemanticModel(
            Vocab vocab,
            Primitives primitives,
            JsonLdOps jsonLd,
            Supplier<GraphCanonizer> canonizeFactory,
            Graph.TypeMappingMatcher typeMatcher,
            Function<Collection<String>, NodeMapper<?>> documentMapper, 
            Map<String, GraphProofMapper> proofMappers) {
        this.vocab = vocab;
        this.primitives = primitives;
        this.jsonLd = jsonLd;

        this.canonizeFactory = canonizeFactory;
        this.typeMatcher = typeMatcher;
        this.documentMapper = documentMapper;
        this.proofMappers = proofMappers;
    }

    @Override
    public SemanticModel.Accessor createAccessor(SequencedCollection<?> context, Map<String, ?> document) {
        return primitives.adapter.createAdapter(
                this,
                context,
                document);
    }

    @Override
    public Document.Updater createUpdater(SequencedCollection<?> context, Map<String, ?> document) {
        return primitives.updater.createUpdater(this, createAccessor(context, document));
    }

    public PayloadGenerator createPayload(SemanticModel.Accessor adapter) {
        return primitives.payload.createPayload(this, adapter);
    }

    public GraphProofCursor createCursor(SemanticModel.Accessor adapter) {
        return primitives.cursor.createCursor(this, adapter);
    }

    public NodeMapper<?> documentMapper(Set<String> types) {
        var mapping = typeMatcher.findBest(types);
        if (mapping != null) {
            return mapping.mapper();
        }
        return null;
//        return documentMapper.apply(types);
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

    public BiFunction<Collection<?>, Map<String, ?>, Map<String, ?>> compact() {
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
                    SequencedCollection<?> context,
                    Map<String, ?> document);
        }

        SequencedCollection<?> context();

        @Override
        Object document();

        Graph documentGraph();

        // returns proof graph ids, might be URI or blank node identifier
        Collection<String> proofGraphs();

        Graph proofGraph(String graph);

        //TODO move to specialized SDGraphAccessor
        Map<String, ?> expandedData();

        Vocab vocab();

        Map<String, ?> source();
    }
    
    public static class DocumentMapping {
        
        Predicate<Collection<?>> context;
        
        TypeMapping typeMapping;
        //TODO Map<String, NodeMapper<?>> propertyMapping

        Map<String, GraphProofMapper> proofMappers;

        String proofPredicate;        
    }
}
