package com.apicatalog.di.std;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import com.apicatalog.trust.model.LexicalModel;
import com.apicatalog.trust.payload.DigestiblePayload;
import com.apicatalog.trust.payload.PayloadGenerator;
import com.apicatalog.trust.processor.MapProcessor;

public class MapPayloadGenerator implements PayloadGenerator {

    private final LexicalModel model;
    private final MapProcessor processor;
    
    private Collection<String> includedProofs;

    public MapPayloadGenerator(
            LexicalModel model,
            MapProcessor processor
            ) {
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

}
