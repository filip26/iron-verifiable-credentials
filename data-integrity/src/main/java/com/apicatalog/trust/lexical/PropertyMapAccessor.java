package com.apicatalog.trust.lexical;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PropertyMapAccessor implements LexicalAccessor {

    private final LexicalModel model;
    private final Collection<?> context;

    private final Map<String, ?> data;
    private final Map<String, Object>[] proofs;

    protected PropertyMapAccessor(
            LexicalModel model,
            Collection<?> context,
            Map<String, ?> data,
            Map<String, Object>[] proofs) {
        this.model = model;
        this.context = context;
        this.data = data;
        this.proofs = proofs;
    }

    public static final PropertyMapAccessor newInstance(
            LexicalModel model,
            Collection<?> context,
            Map<String, ?> document) {

        var data = new LinkedHashMap<>(document);

        var proofs = switch (data.remove(model.vocab().proof())) {
        case Collection<?> col -> col;
        case null -> List.of();
        case Object obj -> List.of(obj);
        };
        
        @SuppressWarnings("unchecked")
        Map<String, Object>[] mapProofs = new Map[proofs.size()];

        int index = 0;
        for (var proof : proofs) {
            if (!(proof instanceof Map<?, ?> rawMap)) {
                throw new IllegalArgumentException("Proof must be a Map");
            }

            @SuppressWarnings("unchecked")
            var map = (Map<String, Object>) rawMap;

            // inject context into a proof
            if (!map.containsKey("@context")) {
                map = new HashMap<>(map);
                map.put("@context", context);
            }

            mapProofs[index++] = map;
        }

        return new PropertyMapAccessor(model, context, data, mapProofs);
    }

    @Override
    public PropertyProofCursor createProofCursor() {
        return model.createCursor(this);
    }

    @Override
    public Map<String, ?> document() {
        return data;
    }

    @Override
    public Map<String, Object> proof(int index) {
        return proofs[index];
    }

    @Override
    public int proofs() {
        return proofs != null ? proofs.length : 0;
    }

    @Override
    public Collection<?> context() {
        return context;
    }
}
