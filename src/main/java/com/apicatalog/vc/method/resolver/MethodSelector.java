package com.apicatalog.vc.method.resolver;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import com.apicatalog.controller.key.VerificationKey;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.proof.Proof;

public class MethodSelector implements VerificationKeyProvider {

    public static Predicate<Proof> EXISTS = Objects::nonNull;

    protected Map<Predicate<Proof>, VerificationKeyProvider> providers;

    protected MethodSelector(Map<Predicate<Proof>, VerificationKeyProvider> providers) {
        this.providers = providers;
    }

    public static Builder create() {
        return new Builder();
    }

    @Override
    public VerificationKey keyFor(Proof proof) throws DocumentError {

        final Optional<VerificationKeyProvider> provider = providers.entrySet().stream()
                .filter(e -> e.getKey().test(proof))
                .map(Entry::getValue)
                .findFirst();

        if (provider.isPresent()) {
            return provider.get().keyFor(proof);
        }
        return null;
    }

    public static class Builder {

        Map<Predicate<Proof>, VerificationKeyProvider> providers;

        public Builder() {
            this.providers = new HashMap<>();
        }

        public Builder with(Predicate<Proof> test, VerificationKeyProvider provider) {
            providers.put(test, provider);
            return this;
        }

        public MethodSelector build() {
            return new MethodSelector(Collections.unmodifiableMap(providers));
        }
    }
}
