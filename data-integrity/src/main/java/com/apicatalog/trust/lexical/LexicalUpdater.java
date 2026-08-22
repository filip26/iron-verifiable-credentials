package com.apicatalog.trust.lexical;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.apicatalog.trust.Document;
import com.apicatalog.trust.model.Model.Vocab;

public class LexicalUpdater implements Document.Updater {

    private final LexicalModel model;
    private final LexicalAccessor accessor;

    private Collection<Map<String, ?>> proofsEntries;

    public LexicalUpdater(LexicalModel model, LexicalAccessor adapter) {
        this.model = model;
        this.accessor = adapter;
    }

    @Override
    public void addProof(Map<String, ?> compacted) {

//        Objects.requireNonNull(context);
        Objects.requireNonNull(compacted);

        if (proofsEntries == null) {
            proofsEntries = new ArrayList<>();
        }
        proofsEntries.add(compacted);
    }

    @Override
    public Map<String, ?> compacted() {

        List<Map<String, ?>> proofs = null;

        // existing proofs
        if (accessor.proofs() > 0) {
            if (proofs == null) {
                proofs = new ArrayList<>(accessor.proofs() + (proofsEntries != null ? proofsEntries.size() : 0));
            }
            for (int i = 0; i < accessor.proofs(); i++) {
                proofs.add(accessor.proof(i));
            }
        }

        final var document = accessor.document();

        // new proofs
        if (proofsEntries != null && !proofsEntries.isEmpty()) {

            if (proofs == null) {
                proofs = new ArrayList<>(proofsEntries.size());
            }

            final var documentContext = document.get("@context");

            for (var proofEntry : proofsEntries) {

                if (documentContext != null && documentContext.equals(proofEntry.get("@context"))) {
                    var proof = new LinkedHashMap<>(proofEntry);
                    proof.remove(model.vocab().context());
                    proofs.add(proof);

                } else {
                    proofs.add(proofEntry);

                }
            }
        }

        if (proofs == null) {
            return document;
        }

        final var compacted = new LinkedHashMap<String, Object>(document.size() + 1);
        compacted.putAll(document);
        compacted.put(model.vocab().proof(), proofs.size() == 1 ? proofs.getFirst() : proofs);

        return compacted;
    }

    @Override
    public PropertyMapPayloadGenerator createPayload() {
        return model.createPayload(accessor);
    }

    @Override
    public Vocab vocab() {
        return model.vocab();
    }
}
