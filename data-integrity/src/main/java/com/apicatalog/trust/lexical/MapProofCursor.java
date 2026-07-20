package com.apicatalog.trust.lexical;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import com.apicatalog.trust.payload.PayloadGenerator;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.proof.ProofCursor;

public class MapProofCursor implements ProofCursor {

    @FunctionalInterface
    public interface Factory {
        MapProofCursor newInstance(
                LexicalModel model,
                MapAdapter processor);
    }

    private final LexicalModel model;
    private final MapAdapter processor;
    private final MapProofReader[] readers;

    private int currentIndex;

    private Proof currentProof;
    private Map<String, Object> currentEntry;
    private PayloadGenerator payloadProvider;

    protected MapProofCursor(
            LexicalModel model,
            MapAdapter processor,
            MapProofReader[] readers) {
        this.model = model;
        this.processor = processor;
        this.readers = readers;

        this.currentProof = null;
        this.currentIndex = -1;
        this.currentEntry = null;
        this.payloadProvider = model.createPayload(processor);
    }

    public static MapProofCursor newInstance(LexicalModel model, MapAdapter processor) {
        var proofs = processor.proofs();

        var mapping = new ArrayList<MapProofReader>(proofs);

        for (var index = 0; index < proofs; index++) {

            var proofMap = processor.proof(index);

            var reader = model.reader((String) proofMap.get("type"));

            mapping.add(reader);
        }

        if (mapping.isEmpty()) {
            return null;
        }

        return new MapProofCursor(model, processor, mapping.toArray(MapProofReader[]::new));
    }

//    public Data data() {
//
//        if (document == null) {
//
//            // TODO add custom document reader
    //// FIXME document = new MapData(payload, model.c14n());
//        }
//
//        return document;
//    }

    @Override
    public boolean isAccepted() {
        return currentEntry != null && readers[currentIndex].isAccepted(currentEntry);
    }

    @Override
    public Proof proof() {
        if (currentProof == null && currentEntry != null) {

            var reader = readers[currentIndex];

            var unsignedProof = new LinkedHashMap<>(currentEntry);
            unsignedProof.remove("proofValue");

            var canonicalProof = model.canonize(unsignedProof);

            // FIXME context!
            payloadProvider.reset();
            currentProof = reader.read(processor.context(), currentEntry, canonicalProof, payloadProvider);
        }

        return currentProof;
    }

    @Override
    public boolean next() {

        if ((currentIndex + 1) == readers.length) {
            return false;
        }

        currentEntry = processor.proof(++currentIndex);
        currentProof = null;
        return true;
    }
}
