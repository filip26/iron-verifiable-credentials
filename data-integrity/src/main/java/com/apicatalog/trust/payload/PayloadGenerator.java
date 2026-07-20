package com.apicatalog.trust.payload;

import java.util.Collection;
import java.util.function.Function;

public interface PayloadGenerator {

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
