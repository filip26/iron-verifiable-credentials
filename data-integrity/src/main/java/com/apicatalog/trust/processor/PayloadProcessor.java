package com.apicatalog.trust.processor;

import java.util.Collection;
import java.util.function.Function;

import com.apicatalog.trust.payload.DigestiblePayload;
import com.apicatalog.trust.payload.GenericPayload;

public interface PayloadProcessor {

    void withProofs(Collection<String> ids);

    default DigestiblePayload digestible() {
        return digestible(GenericPayload::new);
    }

    <T extends DigestiblePayload> T digestible(Function<byte[], T> payloadFactory);

}
