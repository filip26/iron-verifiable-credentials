package com.apicatalog.trust.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import com.apicatalog.trust.processor.MapProcessor;
import com.apicatalog.trust.proof.GraphProofReader;
import com.apicatalog.trust.proof.MapProofCursor;
import com.apicatalog.trust.proof.MapProofReader;
import com.apicatalog.trust.proof.ProofCursor;

public class LexicalModel implements ProcessingModel {

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

//    @Override
//    public ProofCursor createProofCursor(Collection<String> context, Map<String, Object> document) {
//
//        var processor = processorFactory.createProcessor(
//                this,
//                context,
//                document);
//
//        var proofs = processor.proofs();
//
//        var mapping = new ArrayList<MapProofReader>(proofs);
//
//        for (var index = 0; index < proofs; index++) {
//
//            var proofMap = processor.proof(index);
//
//            var reader = proofReaders.get(proofMap.get("type"));
//
//            mapping.add(reader);
//        }
//
//        if (mapping.isEmpty()) {
//            return null;
//        }
//
//        var data = new LinkedHashMap<>(document);
//        data.remove("proof");
//
//        return cursorFactory.newInstance(this, processor, mapping.toArray(MapProofReader[]::new));
//    }

    @Override
    public MapProcessor createProcessor(Map<String, Object> document) {
        return processorFactory.createProcessor(
                this,
                ContextAwareResolver.getContexts(document),
                document);
    }

    public byte[] canonize(Map<String, Object> data) {
        return canonize.apply(data);
    }

//    @Override
//    public String c14n() {
//        return c14n;
//    }

    public MapProofReader reader(String proofType) {
        return proofReaders.get(proofType);
    }
}
