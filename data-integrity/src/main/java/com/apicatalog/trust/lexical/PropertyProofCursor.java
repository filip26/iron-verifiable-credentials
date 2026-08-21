package com.apicatalog.trust.lexical;

import java.util.ArrayList;
import java.util.Map;

import com.apicatalog.trust.payload.PayloadGenerator;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.proof.ProofCursor;

public class PropertyProofCursor implements ProofCursor {

    @FunctionalInterface
    public interface Factory {
        PropertyProofCursor newInstance(
                LexicalModel model,
                LexicalAccessor processor);
    }

    private final LexicalModel model;
    private final LexicalAccessor accessor;
    private final PropertyProofMapper[] readers;

    private int currentIndex;

    private Proof currentProof;
    private Map<String, Object> currentEntry;
    private PayloadGenerator payloadProvider;

    protected PropertyProofCursor(
            LexicalModel model,
            LexicalAccessor processor,
            PropertyProofMapper[] readers) {
        this.model = model;
        this.accessor = processor;
        this.readers = readers;

        this.currentProof = null;
        this.currentIndex = -1;
        this.currentEntry = null;
        this.payloadProvider = model.createPayload(processor);
    }

    public static PropertyProofCursor newInstance(LexicalModel model, LexicalAccessor processor) {
        var proofs = processor.proofs();

        var mapping = new ArrayList<PropertyProofMapper>(proofs);

        for (var index = 0; index < proofs; index++) {

            var proofMap = processor.proof(index);

            var reader = model.reader((String) proofMap.get("type"));

            mapping.add(reader);
        }

        if (mapping.isEmpty()) {
            return null;
        }

        return new PropertyProofCursor(model, processor, mapping.toArray(PropertyProofMapper[]::new));
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
        return currentEntry != null
                && readers[currentIndex] != null
                && readers[currentIndex].accepts(currentEntry);
    }

    @Override
    public Proof proof() {
        if (currentProof == null && currentEntry != null) {

            var reader = readers[currentIndex];

            // FIXME context!
            payloadProvider.reset();
            currentProof = reader.materialize(accessor.context(), currentEntry, model, payloadProvider);
        }

        return currentProof;
    }

    @Override
    public boolean next() {

        if ((currentIndex + 1) == readers.length) {
            return false;
        }

        currentEntry = accessor.proof(++currentIndex);
        currentProof = null;
        return true;
    }
}
