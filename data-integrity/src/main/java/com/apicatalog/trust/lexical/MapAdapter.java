package com.apicatalog.trust.lexical;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.apicatalog.trust.model.Model.Vocab;

public class MapAdapter implements LexicalAdapter {

    private final LexicalModel model;
    private final Collection<String> context;

    private final Map<String, Object> data;
    private final Map<String, Object>[] proofs;

    protected MapAdapter(
            LexicalModel model,
            Collection<String> context,
            Map<String, Object> data,
            Map<String, Object>[] proofs) {
        this.model = model;
        this.context = context;
        this.data = data;
        this.proofs = proofs;
    }

    public static final MapAdapter newInstance(
            LexicalModel model,
            Collection<String> context,
            Map<String, Object> document) {

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

            if (!map.containsKey("@context")) {
                map = new HashMap<>(map);
                map.put("@context", context);
            }

            mapProofs[index++] = map;
        }

        return new MapAdapter(model, context, data, mapProofs);
    }

    @Override
    public MapProofCursor createProofCursor() {
        return model.createCursor(this);
    }

    @Override
    public Map<String, Object> data() {
        return data;
    }

    @Override
    public Map<String, Object> proof(int index) {
        return proofs[index];
    }

    @Override
    public int proofs() {
        return proofs.length;
    }

    @Override
    public Collection<String> context() {
        return context;
    }
}
