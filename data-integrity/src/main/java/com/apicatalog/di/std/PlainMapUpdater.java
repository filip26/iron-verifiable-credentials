package com.apicatalog.di.std;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import com.apicatalog.trust.Document;
import com.apicatalog.trust.lexical.LexicalModel;
import com.apicatalog.trust.lexical.MapAdapter;
import com.apicatalog.trust.lexical.MapPayloadGenerator;
import com.apicatalog.trust.model.Model.Vocab;

public class PlainMapUpdater implements Document.Updater {

    private final LexicalModel model;
    private final MapAdapter adapter;

    private Collection<Map<String, ?>> newProofs;
    private Collection<String> contexts = null;

    public PlainMapUpdater(LexicalModel model, MapAdapter adapter) {
        this.model = model;
        this.adapter = adapter;
    }

    @Override
    public void addProof(Collection<String> context, Map<String, ?> compacted) {
        if (newProofs == null) {
            newProofs = new ArrayList<>();
            contexts = new LinkedHashSet<>(contexts.size() * 2);
        }
        newProofs.add(compacted);
        contexts.addAll(context);
    }

    @Override
    public Map<String, ?> compacted() {

        Collection<Map<String, ?>> proofs = null;

        if (adapter.proofs() > 0) {
            if (proofs == null) {
                proofs = new ArrayList<>(adapter.proofs() + (newProofs != null ? newProofs.size() : 0));
            }
            for (int i = 0; i < adapter.proofs(); i++) {
                proofs.add(adapter.proof(i));
            }
        }

        if (newProofs != null) {
            if (proofs == null) {
                proofs = newProofs;
            } else {
                for (var proof : newProofs) {
                    proofs.add(proof);
                }
            }
        }

        if (proofs == null) {
            return adapter.data();
        }

        var compacted = new LinkedHashMap<>(adapter.data());

        if (contexts != null) {
            compacted.put(model.vocab().context(), merge(adapter.context(), contexts));
        }

        compacted.put(model.vocab().proof(),
                proofs.size() == 1
                        ? proofs.iterator().next()
                        : proofs);

        return compacted;

    }

    @Override
    public MapPayloadGenerator createPayload() {
        return model.createPayload(adapter);
    }

    @Override
    public Vocab vocab() {
        return model.vocab();
    }

    private static Collection<String> merge(Collection<String> documentContext, Collection<String> proofContext) {
        var result = LinkedHashSet.<String>newLinkedHashSet(documentContext.size() + proofContext.size());
        result.addAll(documentContext);
        result.addAll(proofContext);
        return result;
    }
}
