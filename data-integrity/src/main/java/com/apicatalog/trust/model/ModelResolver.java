package com.apicatalog.trust.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

public class ModelResolver {

    Collection<Entry<Predicate<Collection<String>>, Collection<DataModel>>> models;

    private ModelResolver(Collection<Entry<Predicate<Collection<String>>, Collection<DataModel>>> models) {
        this.models = models;
    }

    public static final Builder newBuilder() {
        return new Builder();
    }

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
        case null -> null;
        default ->
            throw new IllegalArgumentException("Invalid @context type: expected a string or a collection of strings");
        };
    }

    public Collection<DataModel> resolve(Collection<String> contexts, Map<String, Object> document) {
        for (var entry : models) {
            if (entry.getKey().test(contexts)) {
                return entry.getValue();
            }
        }
        return List.of();
    }

    public static class Builder {

        Collection<Entry<Predicate<Collection<String>>, Collection<DataModel>>> models;

        public Builder model(
                Predicate<Collection<String>> selector,
                DataModel... model) {

            if (this.models == null) {
                this.models = new ArrayList<>();
            }

            this.models.add(Map.entry(selector, Arrays.asList(model)));
            return this;
        }

        public ModelResolver build() {
            return new ModelResolver(models);
        }
    }

}
