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
    final Map<String, Object> payload;
    final Collection<Entry<Map<String, Object>, ProofMapReader>> proofs;

    Data document;
    Iterator<Entry<Map<String, Object>, ProofMapReader>> iterator;

    Proof currentProof;
    int currentIndex;
    Entry<Map<String, Object>, ProofMapReader> currentEntry;

    public ProofMapCursor(
            TypeSpecificModel model,
            Map<String, Object> payload,
            Collection<Entry<Map<String, Object>, ProofMapReader>> proofs) {
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

            currentProof = reader.read(null, proof, canonicalProof, this::data);
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

    Data data(Collection<String> previous) {
        var data = data();

        if (data.digestiblePayload(previous) == null) {

//TODO select proofs for proof chain
//            var canonizer = model.newCanonizer();
//            var consumer = canonizer.consumer();

            var canonical = model.canonize(payload);
            data.digestiblePayload(new GenericPayload(canonical));

        }
        return data;
    }
}
