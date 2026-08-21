package com.apicatalog.trust.lexical;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import com.apicatalog.trust.Document;
import com.apicatalog.trust.model.Model.Vocab;

public class LexicalUpdater implements Document.Updater {

    private final LexicalModel model;
    private final LexicalAccessor accessor;

    private Collection<Entry<?, Map<String, ?>>> proofsEntries;

    public LexicalUpdater(LexicalModel model, LexicalAccessor adapter) {
        this.model = model;
        this.accessor = adapter;
    }

    @Override
    public void addProof(Object context, Map<String, ?> compacted) {

        Objects.requireNonNull(context);
        Objects.requireNonNull(compacted);

        if (proofsEntries == null) {
            proofsEntries = new ArrayList<>();
        }
        proofsEntries.add(Map.entry(context, compacted));
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
        IO.println("1 > " + accessor.context());
        IO.println("1 > " + accessor.document());
        // new proofs
        if (proofsEntries != null && !proofsEntries.isEmpty()) {

            if (proofs == null) {
                proofs = new ArrayList<>(proofsEntries.size());
            }

            for (var proofEntry : proofsEntries) {

                final Map<String, Object> proof;

                if (!proofEntry.getKey().equals(accessor.context())) {
                    // add @context to the proof
                    proof = new LinkedHashMap<>(proofEntry.getValue());
                    
                    proof.put(model.vocab().context(), proofEntry.getKey());

                } else {
                    proof = (Map<String, Object>) proofEntry.getValue();
                }

                proofs.add(proof);
            }
        }

        if (proofs == null) {
            return accessor.document();
        }

        var document = accessor.document();

        var compacted = new LinkedHashMap<String, Object>(document.size() + 2);
        compacted.putAll(document);

        if (proofs != null && !proofs.isEmpty()) {
            compacted.put(model.vocab().proof(),
                    proofs.size() == 1
                            ? proofs.iterator().next()
                            : proofs);
        }

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

//    private static boolean areCollectionsEqualUnordered(Collection<?> c1, Collection<?> c2) {
//        if (c1.size() != c2.size()) {
//            return false;
//        }
//
//        Map<?, Long> map1 = c1.stream()
//                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));
//
//        Map<?, Long> map2 = c2.stream()
//                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));
//
//        return map1.equals(map2);
//    }

//    private static boolean areSortedEqual(Collection<?> l1, Collection<?> l2) {
//        if (l1.size() != l2.size()) return false;
//        
//        var sorted1 = new ArrayList<Object>(l1);
//        var sorted2 = new ArrayList<Object>(l2);
//        Collections.sort(sorted1);
//        Collections.sort(sorted2);
//        
//        return sorted1.equals(sorted2);
//    }
       
}
