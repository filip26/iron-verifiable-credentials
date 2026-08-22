package com.apicatalog.trust.semantic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;

import com.apicatalog.trust.Document;
import com.apicatalog.trust.model.Model.Vocab;
import com.apicatalog.trust.payload.PayloadGenerator;

public final class GraphUpdater implements Document.Updater {

    @FunctionalInterface
    public interface Factory {
        GraphUpdater createUpdater(SemanticModel model, SemanticModel.Accessor adapter);
    }

    private final SemanticModel model;
    private final SemanticModel.Accessor accessor;

    private Collection<Map<String, ?>> newProofs;
    private Collection<Object> contexts = null;

    public GraphUpdater(SemanticModel model, SemanticModel.Accessor adapter) {
        this.model = model;
        this.accessor = adapter;
    }

    @Override
    public void addProof(Map<String, ?> compacted) {

//        Objects.requireNonNull(context);
        Objects.requireNonNull(compacted);

        if (newProofs == null) {
            newProofs = new ArrayList<>();
        }

        var context = compacted.remove("@context");

        newProofs.add(compacted);

        if (context instanceof Collection<?> sequence) {

            if (contexts == null) {
                contexts = new LinkedHashSet<>(sequence.size() * 3);
            }

            contexts.addAll(sequence);

        } else if (context instanceof String uri) {

            if (contexts == null) {
                contexts = new LinkedHashSet<>();
            }

            contexts.add(uri);

        } else if (context != null) {
            throw new ClassCastException();
        }
    }

    @Override
    public Map<String, ?> compacted() {

        if (newProofs == null) {
            return accessor.source();
        }

        var document = new LinkedHashMap<String, Object>(accessor.source());
        var terms = accessor.vocab();

        var proofs = document.get(terms.proof());

        if (contexts != null) {
            document.put(accessor.vocab().context(), merge(accessor.context(), contexts));
        }

        if (proofs instanceof Collection<?> col) {
            var clone = new ArrayList<Object>(col.size() + newProofs.size());
            clone.addAll(col);
            clone.addAll(newProofs);
            proofs = clone;

        } else if (proofs == null) {
            proofs = newProofs.size() == 1 ? newProofs.iterator().next() : newProofs;

        } else {
            var col = new ArrayList<>(newProofs.size() + 1);
            col.add(proofs);
            col.addAll(newProofs);
            proofs = col;
        }

        document.put(terms.proof(), proofs);

        return document;
    }

    static Collection<?> merge(Collection<?> documentContext, Collection<?> proofContext) {
        var result = LinkedHashSet.<Object>newLinkedHashSet(documentContext.size() + proofContext.size());
        result.addAll(documentContext);
        result.addAll(proofContext);
        return result;
    }

    @Override
    public PayloadGenerator createPayload() {
        return model.createPayload(accessor);
    }

    @Override
    public Vocab vocab() {
        return accessor.vocab();
    }
}
