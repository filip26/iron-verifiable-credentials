package com.apicatalog.trust.model;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import com.apicatalog.trust.proof.ProofCursor;
import com.apicatalog.trust.proof.MapProofReader;
import com.apicatalog.trust.proof.MapProofCursor.Factory;

public class LexicalModel implements Model {

    private final Factory factory;
    private final Map<String, MapProofReader> proofReaders;

    private final String c14n;
    private final Function<Map<String, Object>, byte[]> canonize;

    public LexicalModel(
            Factory factory,
            String c14n,
            Function<Map<String, Object>, byte[]> canonize,
            Map<String, MapProofReader> proofReaders) {
        this.factory = factory;
        this.c14n = c14n;
        this.canonize = canonize;
        this.proofReaders = proofReaders;
    }

    @Override
    public ProofCursor createCursor(Collection<String> context, Map<String, Object> document) {

        var proofProperty = document.get("proof");

        if (proofProperty == null) {
            return null;
        }

        final Collection<?> proofs;

        if (!(proofProperty instanceof Collection<?> col)) {
            proofs = List.of(proofProperty);

        } else if (col.isEmpty()) {
            return null;

        } else {
            proofs = col;
        }

        var mapping = new ArrayList<Entry<Map<String, Object>, MapProofReader>>(proofs.size());

//        boolean cursor = false;

        for (var proof : proofs) {
            if (proof instanceof Map proofMap) {

                MapProofReader reader = proofReaders.get(proofMap.get("type"));

                Map<String, Object> map = proofMap;

                if (!map.containsKey("@context")) {
                    map = new HashMap<String, Object>(proofMap);
                    map.put("@context", context);
                }

                mapping.add(new AbstractMap.SimpleImmutableEntry<>(map, reader));
            }
        }

        if (mapping.isEmpty()) {
            return null;
        }

        var data = new LinkedHashMap<>(document);
        data.remove("proof");

        return factory.newInstance(this, data, mapping);
    }

    public byte[] canonize(Map<String, Object> data) {
        return canonize.apply(data);
    }

    @Override
    public String c14n() {
        return c14n;
    }
}
