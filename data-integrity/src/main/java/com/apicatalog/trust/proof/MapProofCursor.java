package com.apicatalog.trust.proof;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.apicatalog.trust.document.Data;
import com.apicatalog.trust.model.LexicalModel;
import com.apicatalog.trust.processor.MapProcessor;

public class MapProofCursor implements ProofCursor {

    public interface Factory {
        MapProofCursor newInstance(
                LexicalModel model,
                MapProcessor processor,
                Collection<Entry<Map<String, Object>, MapProofReader>> proofReaders);
    }

    private final LexicalModel model;
    private final MapProcessor processor;

//    final Map<String, Object> payload;
    private final Collection<Entry<Map<String, Object>, MapProofReader>> proofs;

    Data document;
    Iterator<Entry<Map<String, Object>, MapProofReader>> iterator;

    Proof currentProof;
    int currentIndex;
    Entry<Map<String, Object>, MapProofReader> currentEntry;

    public MapProofCursor(
            LexicalModel model,
            MapProcessor processor,
            Collection<Entry<Map<String, Object>, MapProofReader>> proofs) {
        this.model = model;
        this.processor = processor;
        this.proofs = proofs;
        this.iterator = proofs.iterator();

        this.currentProof = null;
        this.currentIndex = -1;
        this.currentEntry = null;
    }

    public Data data() {

        if (document == null) {

            // TODO add custom document reader
//FIXME            document = new MapData(payload, model.c14n());
        }

        return document;
    }

    @Override
    public boolean isAccepted() {
        return currentEntry != null && currentEntry.getValue() != null && currentEntry.getKey() != null
                && currentEntry.getValue().isAccepted(currentEntry.getKey());
    }

    @Override
    public Proof proof() {
        if (currentProof == null && currentEntry != null && currentEntry.getValue() != null) {

            var reader = currentEntry.getValue();

            var proof = currentEntry.getKey();

            var unsignedProof = new HashMap<>(proof);
            unsignedProof.remove("proofValue");

            var canonicalProof = model.canonize(unsignedProof);

            currentProof = reader.read(null, proof, canonicalProof, processor);
        }

        return currentProof;
    }

    @Override
    public boolean next() {

        if (!iterator.hasNext()) {
            return false;
        }

        currentEntry = iterator.next();
        currentIndex++;
        currentProof = null;
        return true;
    }
}
