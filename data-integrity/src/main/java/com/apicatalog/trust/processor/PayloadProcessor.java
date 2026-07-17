package com.apicatalog.trust.processor;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import com.apicatalog.trust.payload.DigestiblePayload;
import com.apicatalog.trust.payload.GenericPayload;

public interface PayloadProcessor {

    public interface Factory {


        PayloadProcessor newInstance(Map<String, Object> document);

        // TODO accepted proof types, for configuration dump

        // TODO proof predicate or selector returns proof graph or null
        // Function<String[], String>;

    }

    
    void withProofs(Collection<String> ids);

    default DigestiblePayload digestible() {
        return digestible(GenericPayload::new);
    }

    <T extends DigestiblePayload> T digestible(Function<byte[], T> payloadFactory);

}
