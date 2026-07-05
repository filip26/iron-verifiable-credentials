package com.apicatalog.trust.proof;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.apicatalog.trust.data.Data;
import com.apicatalog.trust.data.GenericPayload;
import com.apicatalog.trust.data.MapData;
import com.apicatalog.trust.model.TypeSpecificModel;

public class ProofMapCursor implements ProofCursor {

    public interface Factory {
        ProofMapCursor newInstance(
                TypeSpecificModel model,
                Map<String, Object> document,
                Collection<Entry<Map<String, Object>, ProofMapReader>> proofReaders);
    }

    final TypeSpecificModel model;
    final Map<String, Object> data;
    final Collection<Entry<Map<String, Object>, ProofMapReader>> proofs;

    Data document;
    Iterator<Entry<Map<String, Object>, ProofMapReader>> iterator;

    Proof currentProof;
    int currentIndex;
    Entry<Map<String, Object>, ProofMapReader> currentEntry;

    public ProofMapCursor(
            TypeSpecificModel model,
            Map<String, Object> data,
            Collection<Entry<Map<String, Object>, ProofMapReader>> proofs) {
        this.model = model;
        this.data = data;
        this.proofs = proofs;
        this.iterator = proofs.iterator();

        this.currentProof = null;
        this.currentIndex = -1;
        this.currentEntry = null;
    }

    public Data data() {

        if (document == null) {
            var canonical = model.canonize(data);
            // TODO add custom document reader
            document = new MapData(data, model.c14n());
            document.digestiblePayload(new GenericPayload(canonical));
        }

        return document;
    }

    @Override
    public boolean isUnknown() {
        return currentEntry == null || currentEntry.getValue() == null || currentEntry.getKey() == null;
    }

    @Override
    public Proof proof() {
        if (currentProof == null && currentEntry != null && currentEntry.getValue() != null) {

            var reader = currentEntry.getValue();

            var proof = currentEntry.getKey();

            var unsignedProof = new HashMap<>(proof);
            unsignedProof.remove(reader.signatureProperty());

            var canonicalProof = model.canonize(unsignedProof);

            currentProof = reader.read(null, proof, canonicalProof, data());
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
