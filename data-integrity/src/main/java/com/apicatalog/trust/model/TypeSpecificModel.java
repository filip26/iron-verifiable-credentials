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

public class TypeSpecificModel implements Model {

    private final Factory factory;
    private final Collection<MapProofReader> proofReaders;

    private final String c14n;
    private final Function<Map<String, Object>, byte[]> canonize;

    public TypeSpecificModel(
            Factory factory,
            String c14n,
            Function<Map<String, Object>, byte[]> canonize,
            Collection<MapProofReader> proofReaders) {
        this.factory = factory;
        this.proofReaders = proofReaders;
        this.c14n = c14n;
        this.canonize = canonize;
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

        boolean cursor = false;
        
        for (var proof : proofs) {
            if (proof instanceof Map proofMap) {

                MapProofReader reader = null;

                for (var proofReader : proofReaders) {
                    if (proofReader.isAccepted((Map<String, Object>) proofMap)) {
                        reader = proofReader;
                        cursor = true;
                        break;
                    }
                }

                Map<String, Object> map = proofMap;

                if (!map.containsKey("@context")) {
                    map = new HashMap<String, Object>(proofMap);
                    map.put("@context", context);
                }

                mapping.add(new AbstractMap.SimpleImmutableEntry<>(map, reader));
            }
        }

        if (!cursor) {
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
