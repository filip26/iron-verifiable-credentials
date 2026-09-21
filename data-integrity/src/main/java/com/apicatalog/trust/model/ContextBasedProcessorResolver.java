package com.apicatalog.trust.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.function.Predicate;

import com.apicatalog.trust.Document.ContextExtractor;
import com.apicatalog.trust.Document.Processor;
import com.apicatalog.trust.semantic.GraphProcessorResources;
import com.apicatalog.trust.semantic.ProcessorFactory;

public final class ContextBasedProcessorResolver {

    private final ContextExtractor contextExtractor;
    private final Predicate<Object>[] predicates;
    private final ProcessorFactory[] factory;

    private ContextBasedProcessorResolver(
            ContextExtractor contextExtractor,
            Predicate<Object>[] predicates,
            ProcessorFactory[] factory) {
        this.contextExtractor = contextExtractor;
        this.predicates = predicates;
        this.factory = factory;
    }

    public Processor resolve(Map<String, ?> document) {
        return resolve(contextExtractor.extract(document), document);
    }

    public Processor resolve(SequencedCollection<?> context, Map<String, ?> document) {
        for (int i = 0; i < factory.length; i++) {
            if (predicates[i].test(context)) {
                return factory[i].newInstance(context, document);
            }
        }
        return null;
    }

    /**
     * Extracts any context from the provided document as a sequenced collection.
     * Includes both referenced contexts (URIs) and inline contexts ({@link Map}.
     * This method is intended for development purposes only.
     *
     * @param document the document map containing the "@context" key
     * @return a sequenced collection containing the context elements, or an empty
     *         list if no context is found
     */
    public static SequencedCollection<?> anyContext(Map<String, ?> document) {
        return switch (document.get("@context")) {
        case SequencedCollection<?> col -> col;
        case Collection<?> col -> List.copyOf(col);
        case Object context -> List.of(context);
        case null -> List.of();
        };
    }

    /**
     * Extracts referenced contexts from the provided document as a sequenced
     * collection of strings. This method enforces that only string references
     * (URIs) are allowed and prohibits inline contexts. This method is intended for
     * production use.
     *
     * @param document the document map containing the "@context" key
     * @return a sequenced collection of context URIs, or an empty list if no
     *         context is found
     * @throws IllegalArgumentException if the context value is not a string, not a
     *                                  collection, or if the collection contains
     *                                  non-string elements
     */
    public static SequencedCollection<String> referencedContext(Map<String, ?> document) {
        return switch (document.get("@context")) {
        case Collection<?> col -> {
            var references = new ArrayList<String>(col.size());
            for (var reference : col) {
                if (!(reference instanceof String uri)) {
                    throw new IllegalArgumentException(
                            "The @context collection contains one or more non-string elements, got " + reference);
                }
                references.add(uri);
            }
            yield references;
        }
        case String context -> List.of(context);
        case null -> List.of();
        default ->
            throw new IllegalArgumentException("Invalid @context type: expected a string or a collection of strings");
        };
    }

    public static final Builder newBuilder() {
        return new Builder();
    }

    // context(...).model(....).model(...).context(...).model(..)...build();
    public static class Builder {

        private ContextExtractor extractor = ContextBasedProcessorResolver::referencedContext;
        private final Collection<Predicate<SequencedCollection<?>>> contextPredicates = new ArrayList<>();
        private final Collection<ProcessorFactory> factories = new ArrayList<>();
        private final Collection<Model> models = new ArrayList<>();

        // FIXME

        public Builder requireReferencedContext() {
            this.extractor = ContextBasedProcessorResolver::referencedContext;
            return this;
        }

        public Builder extractor(ContextExtractor extractor) {
            Objects.requireNonNull(extractor);
            this.extractor = extractor;
            return this;
        }

        public Builder context(Predicate<SequencedCollection<?>> predicate) {
            models.clear();
            return this;
        }
        
        interface Processor {

            // Accessor { documentType(); proofTypes() : Collection<String>); 
            
            // Factory { Processor newProcessor(Accessor accessor, Model model) }
            
            // model()
            
            // documentType()

            // createDocumentMapper()
            // createProofCursor()
            // createPayload()
            // createDocumentUpdater()
            
        }

        public Builder prcessor(Processor resources) {
            
            return this;
        }
        
        public Builder model(Model... models) {

//            if (models.length == 1) {
//                this.contextPredicates.add(contextPredicate);
////                this.models.add(models[0]);
//                return this;
//            }
//
//            this.contextPredicates.add(contextPredicate);
//            this.models.add(new HybridModel(models));
            return this;
        }

        @SuppressWarnings("unchecked")
        public ContextBasedProcessorResolver build() {
            return new ContextBasedProcessorResolver(
                    extractor,
                    contextPredicates.toArray(Predicate[]::new),
                    factories.toArray(ProcessorFactory[]::new));
        }
    }

}
