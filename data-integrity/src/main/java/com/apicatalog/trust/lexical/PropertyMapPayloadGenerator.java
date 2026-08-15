package com.apicatalog.trust.lexical;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import com.apicatalog.trust.payload.DigestiblePayload;
import com.apicatalog.trust.payload.PayloadGenerator;

public class PropertyMapPayloadGenerator implements PayloadGenerator {

    private final LexicalModel model;
    private final LexicalAccessor accessor;

    private Collection<String> includedProofs;

    public PropertyMapPayloadGenerator(
            LexicalModel model,
            LexicalAccessor processor) {
        this.model = model;
        this.accessor = processor;
        this.includedProofs = List.of();
    }

    @Override
    public <T extends DigestiblePayload> T digestible(Function<byte[], T> payloadFactory) {

        if (includedProofs.isEmpty()) {
            // TODO cache
            var canonical = model.canonize(accessor.data());
            return payloadFactory.apply(canonical);
        }

        var proofs = new ArrayList<Map<String, Object>>(includedProofs.size());

        for (int i = 0; i < accessor.proofs(); i++) {

            var proof = accessor.proof(i);
            if (includedProofs.contains(proof.get(model.vocab().id()))) {
                proofs.add(proof);
            }

        }

        var document = new HashMap<>(accessor.data());
        document.put(model.vocab().proof(), proofs);

        var canonical = model.canonize(document);
        return payloadFactory.apply(canonical);
    }

    @Override
    public void withProofs(Collection<String> ids) {
        Objects.requireNonNull(ids);
        this.includedProofs = ids;
    }

    @Override
    public void reset() {
        this.includedProofs = List.of();
    }
}
