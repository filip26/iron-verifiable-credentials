package com.apicatalog.trust.model;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import com.apicatalog.trust.processor.MapProcessor;
import com.apicatalog.trust.processor.PayloadProcessor;
import com.apicatalog.trust.proof.MapProofCursor;
import com.apicatalog.trust.proof.MapProofReader;
import com.apicatalog.trust.proof.ProofCursor;

public class LexicalModel implements DataModel {

    private final MapProcessor.Factory processorFactory;
    private final MapProofCursor.Factory cursorFactory;
    private final Map<String, MapProofReader> proofReaders;

    private final String c14n;
    private final Function<Map<String, Object>, byte[]> canonize;

    public LexicalModel(
            MapProcessor.Factory processorFactory,
            MapProofCursor.Factory cursorFactory,
            String c14n,
            Function<Map<String, Object>, byte[]> canonize,
            Map<String, MapProofReader> proofReaders) {
        this.processorFactory = processorFactory;
        this.cursorFactory = cursorFactory;
        this.c14n = c14n;
        this.canonize = canonize;
        this.proofReaders = proofReaders;
    }

    @Override
    public ProofCursor createProofCursor(Collection<String> context, Map<String, Object> document) {

        var processor = processorFactory.newInstance(
                this,
                context,
                document);

        var proofs = processor.proofs();

        var mapping = new ArrayList<Entry<Map<String, Object>, MapProofReader>>(proofs.size());

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

        return cursorFactory.newInstance(this, processor, mapping);
    }

    public byte[] canonize(Map<String, Object> data) {
        return canonize.apply(data);
    }

    @Override
    public String c14n() {
        return c14n;
    }

    @Override
    public PayloadProcessor createProcessor(Map<String, Object> document) {
        return processorFactory.newInstance(
                this,
                ModelResolver.getContexts(document),
                document);
    }
}
