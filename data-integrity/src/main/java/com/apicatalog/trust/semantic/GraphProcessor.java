package com.apicatalog.trust.semantic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.apicatalog.di.DataIntegrity;
import com.apicatalog.di.DataIntegrity.SemanticModelBuilder;
import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.di.proof.Ed25519Signature2020;
import com.apicatalog.di.suite.CryptoSuite;
import com.apicatalog.trust.model.Model.Vocab;
import com.apicatalog.trust.semantic.Graph.NodeMapper;
import com.apicatalog.trust.semantic.Graph.TypeMapping;
import com.apicatalog.trust.semantic.SemanticModel.GraphCanonizer;
import com.apicatalog.trust.semantic.SemanticModel.QuadConsumer;

public class GraphProcessor {

    SemanticModel.Accessor.Factory adapter;
    GraphUpdater.Factory updater;
    GraphProofCursor.Factory cursor;
    GraphPayloadGenerator.Factory payload;

    Function<Map<String, ?>, SequencedCollection<?>> expand;
    BiFunction<Collection<?>, Map<String, ?>, Map<String, ?>> compact;
    BiConsumer<Object, QuadConsumer> tordf;

    private Supplier<GraphCanonizer> canonizeFactory;

    public SemanticModel.Accessor accessor(
            SemanticModel model,
            SequencedCollection<?> context,
            Map<String, ?> document) {
        return adapter.createAdapter(
                model,
                context,
                document);
    }

//
//    public Document.Updater createUpdater(SequencedCollection<?> context, Map<String, ?> document) {
//        return updater.createUpdater(this, createAccessor(context, document));
//    }
//
//    public PayloadGenerator createPayload(SemanticModel.Accessor adapter) {
//        return payload.createPayload(this, adapter);
//    }
//
//    public GraphProofCursor createCursor(SemanticModel.Accessor adapter) {
//        return cursor.createCursor(this, adapter);
//    }

    public GraphCanonizer newCanonizer() {
        return canonizeFactory.get();
    }

    public void tordf(Object document, QuadConsumer consumer) {
        tordf.accept(document, consumer);
    }

    public SequencedCollection<?> expand(Map<String, ?> document) {
        return expand.apply(document);
    }

    // TODO belongs to SDGraphProcessor
    public Map<String, ?> compact(Collection<?> context, Map<String, ?> expanded) {
        return compact.apply(context, expanded);
    }

    public static Builder newBuilder(String c14n) {
        return new Builder(c14n);
    }

    public static class Builder {

        private final String c14n;

        private Supplier<GraphCanonizer> c14nFactory;

        private SemanticModel.Accessor.Factory accessorFactory;
        private GraphUpdater.Factory updaterFactory;
        private GraphProofCursor.Factory cursorFactory;
        private GraphPayloadGenerator.Factory payloadFactory;

        private BiConsumer<Object, QuadConsumer> tordf;
        private BiFunction<Collection<?>, Map<String, ?>, Map<String, ?>> compact;
        private Function<Map<String, ?>, SequencedCollection<?>> expand;

        private Map<String, Supplier<GraphCanonizer>> proofC14n = Map.of();

        private Builder(String c14n) {
            this.c14n = c14n;
            // default processors
            this.accessorFactory = GraphAccessor::newInstance;
            this.updaterFactory = GraphUpdater::new;
            this.cursorFactory = GraphProofCursor::newInstance;
            this.payloadFactory = GraphPayloadGenerator::new;
        }

        public Builder c14n(Supplier<GraphCanonizer> c14nFactory) {
            this.c14nFactory = c14nFactory;
            return this;
        }

        public Builder c14n(String proofType, Supplier<GraphCanonizer> c14nFactory) {
            if (this.proofC14n.isEmpty()) {
                this.proofC14n = new HashMap<>();
            }
            this.proofC14n.put(proofType, c14nFactory);
            return this;
        }

        public Builder expand(Function<Map<String, ?>, SequencedCollection<?>> expand) {
            this.expand = expand;
            return this;
        }

        public Builder compact(
                BiFunction<Collection<?>, Map<String, ?>, Map<String, ?>> compact) {
            this.compact = compact;
            return this;
        }

        public Builder tordf(BiConsumer<Object, QuadConsumer> tordf) {
            this.tordf = tordf;
            return this;
        }

        public Builder cursor(GraphProofCursor.Factory factory) {
            this.cursorFactory = factory;
            return this;
        }

        public Builder accessor(SemanticModel.Accessor.Factory factory) {
            this.accessorFactory = factory;
            return this;
        }

        public Builder updater(GraphUpdater.Factory factory) {
            this.updaterFactory = factory;
            return this;
        }

        public Builder payload(GraphPayloadGenerator.Factory factory) {
            this.payloadFactory = factory;
            return this;
        }

        public GraphProcessor build() {

            if (c14nFactory == null) {
                throw new IllegalStateException();
            }

            // FIXME
            return null;
//            var processor = new GraphProcessor(
//                    accessorFactory,
//                    updaterFactory,
//                    cursorFactory,
//                    payloadFactory,
//                    expand,
//                    compact,
//                    tordf,
//                    c14nFactory
//                    );

//            return new SemanticModel(
//                    new Vocab(
//                            "@context",
//                            proofPredicate,
//                            null,
//                            Graph.PREDICATE_TYPE),
//                    processor,
//                    typeMapping != null && !typeMapping.isEmpty()
//                            ? new Graph.TypeMappingMatcher(typeMapping)
//                            : null,
//                    documentMapper,
//                    proofMappers);
        }
    }
}
