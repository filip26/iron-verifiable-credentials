package com.apicatalog.trust.semantic;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.apicatalog.trust.Document;
import com.apicatalog.trust.payload.PayloadGenerator;
import com.apicatalog.trust.semantic.SemanticModel.GraphCanonizer;
import com.apicatalog.trust.semantic.SemanticModel.QuadConsumer;

public class GraphProcessor {

    private final SemanticModel.Accessor.Factory adapter;
    private final GraphUpdater.Factory updater;
    private final GraphProofCursor.Factory cursor;
    private final GraphPayloadGenerator.Factory payload;

    private final Function<Map<String, ?>, SequencedCollection<?>> expand;
    private final BiFunction<Collection<?>, Map<String, ?>, Map<String, ?>> compact;
    private final BiConsumer<Object, QuadConsumer> tordf;
    
    private final Supplier<GraphCanonizer> canonizerFactory;

    private GraphProcessor(
            SemanticModel.Accessor.Factory adapter,
            GraphUpdater.Factory updater,
            GraphProofCursor.Factory cursor,
            GraphPayloadGenerator.Factory payload,

            Function<Map<String, ?>, SequencedCollection<?>> expand,
            BiFunction<Collection<?>, Map<String, ?>, Map<String, ?>> compact,
            BiConsumer<Object, QuadConsumer> tordf,
            
            Supplier<GraphCanonizer> canonizerFactory) {
        this.adapter = adapter;
        this.updater = updater;
        this.cursor = cursor;
        this.payload = payload;
        
        this.expand = expand;
        this.compact = compact;
        this.tordf = tordf;
        
        this.canonizerFactory = canonizerFactory;
    }

    public SemanticModel.Accessor accessor(
            SemanticModel model,
            SequencedCollection<?> context,
            Map<String, ?> document) {
        return adapter.createAdapter(
                model,
                context,
                document);
    }


    public Document.Updater updater(
            SemanticModel model,
            SemanticModel.Accessor adapter) {
        return updater.createUpdater(model, adapter);
    }

    public PayloadGenerator createPayload(
            SemanticModel model,
            SemanticModel.Accessor adapter) {
        return payload.createPayload(model, adapter);
    }

    public GraphProofCursor createCursor(
            SemanticModel model,
            SemanticModel.Accessor adapter) {
        return cursor.createCursor(model, adapter);
    }

    public GraphCanonizer newCanonizer() {
        return canonizerFactory.get();
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

            return new GraphProcessor(
                    accessorFactory,
                    updaterFactory,
                    cursorFactory,
                    payloadFactory,
                    expand,
                    compact,
                    tordf,
                    c14nFactory);
        }
    }
}
