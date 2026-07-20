package com.apicatalog.di.std;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import com.apicatalog.trust.Document;
import com.apicatalog.trust.model.Model.Vocab;
import com.apicatalog.trust.payload.PayloadGenerator;
import com.apicatalog.trust.semantic.GraphAdapter;
import com.apicatalog.trust.semantic.SemanticModel;

public final class JsonLdUpdater implements Document.Updater {

    private final SemanticModel model;
    private final GraphAdapter adapter;

    private Collection<Map<String, ?>> newProofs;
    private Collection<String> contexts = null;

    public JsonLdUpdater(SemanticModel model, GraphAdapter adapter) {
        this.model = model;
        this.adapter = adapter;
    }

    @Override
    public void addProof(Collection<String> context, Map<String, ?> compacted) {
        if (newProofs == null) {
            newProofs = new ArrayList<>();
            contexts = new LinkedHashSet<>(context.size() * 2);
        }
        newProofs.add(compacted);
        contexts.addAll(context);
    }

    @Override
    public Map<String, ?> compacted() {

        if (newProofs == null) {
            return adapter.source();
        }

        // TODO Auto-generated method stub
        var document = new LinkedHashMap<String, Object>(adapter.source());
        var terms = adapter.vocab();

        var proofs = document.get(terms.proof());

        if (contexts != null) {
            document.put(adapter.vocab().context(), merge(adapter.context(), contexts));
        }

        if (proofs instanceof Collection<?> col) {
            var clone = new ArrayList<Object>(col);
            clone.addAll(newProofs);
            proofs = col;

        } else if (proofs == null) {
            proofs = newProofs.size() == 1 ? newProofs.iterator().next() : newProofs;

        } else {
            var col = new ArrayList<>();
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
