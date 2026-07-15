package com.apicatalog.di.sd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import com.apicatalog.multibase.Multibase;
import com.apicatalog.trust.model.DataModel;
import com.apicatalog.trust.model.SemanticModel;

public class SDBaseDocument implements SDPayload {

    byte[] base;

    byte[][] redactable;
    int[] redactableIndices;

    Collection<String> mandatoryPointers;
    int[] mandatoryIndices;

    byte[] hmacKey;

    Map<String, Object> compacted;
    List<String> canonized;

    Map<String, String> labels;

    SemanticModel model;

    public SDDerivedDocument derive(Collection<String> selectors) {

        var combinedPointers = new LinkedHashSet<String>(mandatoryPointers.size() + selectors.size());
        combinedPointers.addAll(mandatoryPointers);
        combinedPointers.addAll(selectors);
        IO.println("c > " + combinedPointers);

        IO.println("m > " + Arrays.toString(mandatoryIndices));

        var selection = Pointer.select(compacted, combinedPointers);

        var selectedNQuads = new HashSet<String>();

        var c14n = model.newCanonizer();
        var consumer = c14n.consumer();

        model.tordf().accept(selection, ((subject, predicate, object, datatype, language, direction, graph) -> {

            var s = subject;
            if (s.startsWith(Skolemizer.URN_PREFIX)) {
                s = labels.get("_:" + s.substring(Skolemizer.URN_PREFIX.length()));
            }
            var o = object;
            if (o.startsWith(Skolemizer.URN_PREFIX)) {
                o = labels.get("_:" + o.substring(Skolemizer.URN_PREFIX.length()));
            }

            var nquad = c14n.toNQuad(s, predicate, o, datatype, language, direction, graph);

            consumer.accept(s, predicate, o, datatype, language, direction, graph);
            selectedNQuads.add(nquad);
        }));

        c14n.canonize();
        
        var selectionLabels = HashMap.<Integer, byte[]>newHashMap(c14n.labels().size());
        for (var label : c14n.labels().entrySet()) {
            selectionLabels.put(
                    Integer.parseInt(label.getValue().substring("_:c14n".length())),
                    Multibase.BASE_64_URL.decode(label.getKey().substring("_:".length()))
                    );
        }

        int index = 0;

        var selectedIndices = new int[selectedNQuads.size()];
        int selectedIndex = 0;

        for (var nquad : canonized) {
            if (selectedNQuads.contains(nquad)) {
                selectedNQuads.remove(nquad);

                selectedIndices[selectedIndex++] = index;

                // all selected indices found
                if (selectedNQuads.isEmpty()) {
                    break;
                }
            }
            index++;
        }

        IO.println("s > " + Arrays.toString(selectedIndices));

        IO.println(selection);

        var indices = mandatory(selectedIndices, mandatoryIndices);

        IO.println("r > " + Arrays.toString(redactableIndices));

//        Arrays.sort(mandatoryIndices);
//TODO        var disclosedPayload = new byte[selectedIndices.length - mandatoryIndices.length][];
        var disclosedPayload = new ArrayList<byte[]>(selectedIndices.length - mandatoryIndices.length);
        var signatureIndex = 0;

        var signatureIndices = new int[selectedIndices.length - mandatoryIndices.length];
        
        for (var si : selectedIndices) {
            var ri = Arrays.binarySearch(redactableIndices, si);
            if (ri >= 0) {
//                IO.println("sig > " + si + ", " + ri);
// FIXME               disclosedPayload[signatureIndex++] = redactable[ri];
                disclosedPayload.add(redactable[ri]);
                signatureIndices[signatureIndex++] = ri;
            }
        }

//        if (signatureIndex < disclosedPayload.size()) {
//            throw new IllegalStateException();
//        }

//        Arrays.asList(disclosedPayload).stream().map(String::new).forEach(IO::println);

        IO.println("i > " + Arrays.toString(indices));
        IO.println("i > " + selectionLabels);
        
        var derived = new SDDerivedDocument();
        derived.base = base;
        derived.disclosed = disclosedPayload;
        derived.disclosedIndices = signatureIndices;
//        derived.
//        derived.disclosed = nonMandatory;

        return derived;
    }

    private static int[] mandatory(int[] combined, int[] mandatory) {

        final var indices = new int[mandatory.length];

        int index = 0;
        int relative = 0;

        Arrays.sort(mandatory);

        for (int key : combined) {
            if (Arrays.binarySearch(mandatory, key) >= 0) {
                indices[index++] = relative;
            }
            relative++;
        }
        return indices;
    }

    @Override
    public byte[] canonicalPayload() {
        return base;
    }

    @Override
    public Collection<byte[]> redactablePayload() {
        // FIXME
        return Arrays.asList(redactable);
    }

    public Collection<String> mandatoryPointers() {
        return mandatoryPointers;
    }

    public byte[] hmacKey() {
        return hmacKey;
    }

    @Override
    public String c14n() {
        return DataModel.C14N_RDFC;
    }
}
