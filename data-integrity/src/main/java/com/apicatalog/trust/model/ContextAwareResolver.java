package com.apicatalog.trust.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class ContextAwareResolver {

    private final Predicate<Collection<String>>[] predicates;
    private final ProcessingModel[] models;

    private ContextAwareResolver(
            Predicate<Collection<String>>[] predicates,
            ProcessingModel[] models) {
        this.predicates = predicates;
        this.models = models;
    }

    // TODO must not be static, the resolver should have been configured with
    // "@context" key, or context extractos? -> getJsonLdContext(...)
    public static Collection<String> getContexts(Map<String, Object> document) {
        return switch (document.get("@context")) {
        case Collection<?> col -> col.stream()
                .map(item -> {
                    if (item instanceof String s) {
                        return s;
                    }
                    throw new IllegalArgumentException(
                            "The @context collection contains one or more non-string elements");
                })
                .toList();
        case String context -> List.of(context);
        case null -> List.of();
        default ->
            throw new IllegalArgumentException("Invalid @context type: expected a string or a collection of strings");
        };
    }

    public ProcessingModel resolve(Collection<String> contexts, Map<String, Object> document) {
        for (int i = 0; i < models.length; i++) {
            if (predicates[i].test(contexts)) {
                return models[i];
            }
        }
        return null;
    }

    public static final Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Collection<Predicate<Collection<String>>> predicates = new ArrayList<>();;
        private final Collection<ProcessingModel> models = new ArrayList<>();

        public Builder model(
                Predicate<Collection<String>> selector,
                ProcessingModel... models) {

            if (models.length == 1) {
                this.predicates.add(selector);
                this.models.add(models[0]);
                return this;
            }

            this.predicates.add(selector);
            this.models.add(new HybridAdapterModel(models));
            return this;
        }

        @SuppressWarnings("unchecked")
        public ContextAwareResolver build() {
            return new ContextAwareResolver(
                    predicates.toArray(Predicate[]::new),
                    models.toArray(ProcessingModel[]::new));
        }
    }

}
