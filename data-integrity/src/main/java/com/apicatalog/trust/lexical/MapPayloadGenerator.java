package com.apicatalog.trust.lexical;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import com.apicatalog.trust.payload.DigestiblePayload;
import com.apicatalog.trust.payload.PayloadGenerator;

public class MapPayloadGenerator implements PayloadGenerator {

    private final LexicalModel model;
    private final MapAdapter processor;

    private Collection<String> includedProofs;

    public MapPayloadGenerator(
            LexicalModel model,
            MapAdapter processor) {
        this.model = model;
        this.processor = processor;
        this.includedProofs = null;
    }

    @Override
    public <T extends DigestiblePayload> T digestible(Function<byte[], T> payloadFactory) {

        Map<String, Object> target = processor.data();

        // TODO ids

        var canonical = model.canonize(target);
        return payloadFactory.apply(canonical);
    }

    @Override
    public void withProofs(Collection<String> ids) {
        this.includedProofs = ids;
    }

    @Override
    public void reset() {
        this.includedProofs = null;
    }
}
