package com.apicatalog.trust.lexical;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import com.apicatalog.trust.Document;
import com.apicatalog.trust.model.Model.Vocab;

public class MapUpdater implements Document.Updater {

    private final LexicalModel model;
    private final LexicalAdapter adapter;

    private Collection<Map<String, ?>> newProofs;
    private Collection<String> contexts = null;

    public MapUpdater(LexicalModel model, LexicalAdapter adapter) {
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
