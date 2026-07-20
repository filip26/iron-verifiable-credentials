package com.apicatalog.di.std;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.tree.io.Tree.NodeContext;
import com.apicatalog.tree.io.java.NativeComposer;
import com.apicatalog.trust.payload.PayloadGenerator;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.semantic.GraphAdapter;
import com.apicatalog.trust.semantic.GraphUpdater;
import com.apicatalog.trust.semantic.SemanticModel;

public final class JsonLdUpdater implements GraphUpdater {

    private final SemanticModel model;
    private final GraphAdapter adapter;

    private Collection<Proof> proofsToAdd;

    public JsonLdUpdater(SemanticModel model, GraphAdapter adapter) {
        this.model = model;
        this.adapter = adapter;
    }

    @Override
    public void addProof(Proof proof) {
        if (proofsToAdd == null) {
            proofsToAdd = new ArrayList<Proof>();
        }
        proofsToAdd.add(proof);
    }

    @Override
    public Map<String, ?> compacted() {
        // TODO Auto-generated method stub
        var document = new LinkedHashMap<String, Object>(adapter.compacted());
        var terms = adapter.keys();

        var proofs = document.get(terms.proof());

        var composer = new NativeComposer<Map<String, ? extends Object>>();
//        composer.beginSequence(NodeContext.ROOT);
        for (var proof : proofsToAdd) {

            // TODO use id, type, pass it to writer
            DataIntegrityProof.write((DataIntegrityProof) proof, composer);

//          if (proofDraft.context() != null && !proofDraft.context().isEmpty()) {
//              document.put("@context", merge(context, proofDraft.context()));
//          }

        }
//        composer.endSequence(NodeContext.ROOT);

        // TODO remove, hide in document handler interface
        var proofMap = composer.compose();

        if (proofs instanceof Collection col) {
            var clone = new ArrayList<>(col);
            col.add(proofMap);
            proofs = col;

        } else if (proofs == null) {
            proofs = proofMap;

        } else {
            var col = new ArrayList<>();
            col.add(proofs);
            col.add(proofMap);
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

}
