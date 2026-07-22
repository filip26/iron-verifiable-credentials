package com.apicatalog.trust.semantic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import com.apicatalog.trust.Document;
import com.apicatalog.trust.model.Model.Vocab;
import com.apicatalog.trust.payload.PayloadGenerator;

public final class GraphUpdater implements Document.Updater {

    public interface Factory {
        GraphUpdater createUpdater(SemanticModel model, SemanticAdapter adapter);
    }

    private final SemanticModel model;
    private final SemanticAdapter adapter;

    private Collection<Map<String, ?>> newProofs;
    private Collection<String> contexts = null;

    public GraphUpdater(SemanticModel model, SemanticAdapter adapter) {
        this.model = model;
        this.adapter = adapter;
    }

    @Override
    public void addProof(Collection<String> context, Map<String, ?> compacted) {
        if (newProofs == null) {
            newProofs = new ArrayList<>();
        }
        newProofs.add(compacted);

        if (context != null) {
            if (contexts == null) {
                contexts = new LinkedHashSet<>(context.size() * 2);
            }
            contexts.addAll(context);
        }
    }

    @Override
    public Map<String, ?> compacted() {

        if (newProofs == null) {
            return adapter.source();
        }

        var document = new LinkedHashMap<String, Object>(adapter.source());
        var terms = adapter.vocab();

        var proofs = document.get(terms.proof());

        if (contexts != null) {
            document.put(adapter.vocab().context(), merge(adapter.context(), contexts));
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

    static Collection<String> merge(Collection<String> documentContext, Collection<String> proofContext) {
        var result = LinkedHashSet.<String>newLinkedHashSet(documentContext.size() + proofContext.size());
        result.addAll(documentContext);
        result.addAll(proofContext);
        return result;
    }

    @Override
    public PayloadGenerator createPayload() {
        return model.createPayload(adapter);
    }

    @Override
    public Vocab vocab() {
        return adapter.vocab();
    }
}
