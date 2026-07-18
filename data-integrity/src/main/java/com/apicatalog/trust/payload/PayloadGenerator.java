package com.apicatalog.trust.payload;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

public interface PayloadGenerator {

    public interface Factory {

        PayloadGenerator createPayload(Map<String, Object> document);

        // TODO accepted proof types, for configuration dump

        // TODO proof predicate or selector returns proof graph or null
        // Function<String[], String>;

    }

    void withProofs(Collection<String> ids);

    default DigestiblePayload digestible() {
        return digestible(GenericPayload::new);
    }

    <T extends DigestiblePayload> T digestible(Function<byte[], T> payloadFactory);

    /**
     * resets the provider state, but might cache vanilla digestible payload for
     * re-use
     * 
     */
    default void reset() {
        throw new UnsupportedOperationException();
    }
}
