package com.apicatalog.trust.proof;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.apicatalog.trust.document.Data;
import com.apicatalog.trust.document.MapData;
import com.apicatalog.trust.model.LexicalModel;
import com.apicatalog.trust.payload.DigestiblePayload;
import com.apicatalog.trust.payload.GenericPayload;
import com.apicatalog.trust.payload.RedactablePayload;
import com.apicatalog.trust.processor.PayloadProcessor;
import com.apicatalog.trust.signature.Signature;

public class MapProofCursor implements ProofCursor, PayloadProcessor {

    public interface Factory {
        MapProofCursor newInstance(
                LexicalModel model,
                Map<String, Object> document,
                Collection<Entry<Map<String, Object>, MapProofReader>> proofReaders);
    }

    final LexicalModel model;
    final Map<String, Object> payload;
    final Collection<Entry<Map<String, Object>, MapProofReader>> proofs;

    Data document;
    Iterator<Entry<Map<String, Object>, MapProofReader>> iterator;

    Proof currentProof;
    int currentIndex;
    Entry<Map<String, Object>, MapProofReader> currentEntry;

    public MapProofCursor(
            LexicalModel model,
            Map<String, Object> payload,
            Collection<Entry<Map<String, Object>, MapProofReader>> proofs) {
        this.model = model;
        this.payload = payload;
        this.proofs = proofs;
        this.iterator = proofs.iterator();

        this.currentProof = null;
        this.currentIndex = -1;
        this.currentEntry = null;
    }

    public Data data() {

        if (document == null) {

            // TODO add custom document reader
            document = new MapData(payload, model.c14n());
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

            currentProof = reader.read(null, proof, canonicalProof, this);
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

//    Data data(Collection<String> previous) {
//        var data = data();
//
//        if (data.digestiblePayload(previous) == null) {
//
////TODO select proofs for proof chain
////            var canonizer = model.newCanonizer();
////            var consumer = canonizer.consumer();
//
//
//        }
//        return data;
//    }

    @Override
    public void withProofs(Collection<String> ids) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public DigestiblePayload digestible() {
        var canonical = model.canonize(payload);
        return new GenericPayload(canonical);
    }
}
