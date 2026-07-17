package com.apicatalog.trust.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

public class ContextAwareResolver {

    Collection<Entry<Predicate<Collection<String>>, Collection<ProcessingModel>>> models;

    private ContextAwareResolver(
            Collection<Entry<Predicate<Collection<String>>, Collection<ProcessingModel>>> models) {
        this.models = models;
    }

    public static final Builder createBuilder() {
        return new Builder();
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

    public Collection<ProcessingModel> resolve(Collection<String> contexts, Map<String, Object> document) {
        for (var entry : models) {
            if (entry.getKey().test(contexts)) {
                return entry.getValue();
            }
        }
        return List.of();
    }

    public static class Builder {

        Collection<Entry<Predicate<Collection<String>>, Collection<ProcessingModel>>> models;

        public Builder model(
                Predicate<Collection<String>> selector,
                ProcessingModel... model) {

            if (this.models == null) {
                this.models = new ArrayList<>();
            }

            this.models.add(Map.entry(selector, Arrays.asList(model)));
            return this;
        }

        public ContextAwareResolver build() {
            return new ContextAwareResolver(models);
        }
    }

}
