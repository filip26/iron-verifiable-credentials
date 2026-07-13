package com.apicatalog.di.sd;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import com.apicatalog.trust.model.SemanticModel;
import com.apicatalog.trust.model.SemanticModel.QuadConsumer;
import com.apicatalog.trust.payload.DigestiblePayload;
import com.apicatalog.trust.payload.RedactablePayload;
import com.apicatalog.trust.processor.GraphProcessor;

public class SDGraphProcessor implements GraphProcessor {

    private final SemanticModel model;
    private final Collection<String> context;
    private final Map<String, Object> document;

    private Map<String, Object> expandedDocument;
    private Dataset dataset;

    public SDGraphProcessor(
            SemanticModel model,
            Collection<String> context,
            Map<String, Object> document) {
        this.model = model;
        this.context = context;
        this.document = document;

        this.expandedDocument = null;
        this.dataset = null;
    }

    @Override
    public Collection<String> contexts() {
        return context;
    }

    @Override
    public Collection<String[]> proof(String graph) {
        lazyInit();
        return dataset.graphs.get(graph);
    }

    @Override
    public String proofType(String graph) {
        lazyInit();
        return dataset.proofTypes.get(graph);
    }

    @Override
    public Collection<String> proofs() {
        lazyInit();
        return dataset.proofGraphs;
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub

    }

    @Override
    public RedactablePayload redactable(Collection<String> mandatoryPointers, Map<String, Object> options) {

        var hmacKey = (byte[]) options.get("HMAC_KEY"); // FIXME

        lazyInit();

        var skolemized = Skolemizer.skolemize(expandedDocument);

        var compacted = model.compact().apply(context, skolemized);

//        IO.println("compacted > " + compacted);

        var selection = Selection.select(compacted, mandatoryPointers);

//        IO.println("selection > " + selection);

        var canonizer = model.newCanonizer();

        var consumer = canonizer.consumer();

        model.tordf().accept(skolemized, ((subject, predicate, object, datatype, language, direction, graph) -> {

            var s = subject;
            if (s.startsWith(Skolemizer.URN_PREFIX)) {
                s = "_:" + s.substring(Skolemizer.URN_PREFIX.length());
            }
            var o = object;
            if (o.startsWith(Skolemizer.URN_PREFIX)) {
                o = "_:" + o.substring(Skolemizer.URN_PREFIX.length());
            }

            consumer.accept(s, predicate, o, datatype, language, direction, graph);
        }));

        var canonized = new ArrayList<String>();

        // TODO read from model
        var hmac = HmacIdProvider.newInstance(hmacKey);

        canonizer.canonize((subject, predicate, object, datatype, language, direction, graph) -> {

            var s = subject;
            if (s.startsWith("_:")) {
                s = hmac.getHmacId(s);
            }
            var o = object;
            if (o.startsWith("_:")) {
                o = hmac.getHmacId(o);
            }

            canonized.add(canonizer.toNQuad(s, predicate, o, datatype, language, direction, graph));
        });

        Collections.sort(canonized);

        var selectedNQuads = new HashSet<String>(selection.size());

        model.tordf().accept(selection, ((subject, predicate, object, datatype, language, direction, graph) -> {

            var s = subject;
            if (s.startsWith(Skolemizer.URN_PREFIX)) {
                s = hmac.mapping.get(canonizer.labels().get("_:" + s.substring(Skolemizer.URN_PREFIX.length())));
            }
            var o = object;
            if (o.startsWith(Skolemizer.URN_PREFIX)) {
                o = hmac.mapping.get(canonizer.labels().get("_:" + o.substring(Skolemizer.URN_PREFIX.length())));
            }

            var nquad = canonizer.toNQuad(s, predicate, o, datatype, language, direction, graph);

            selectedNQuads.add(nquad);
        }));

        int index = 0;

        var mandatory = new int[selectedNQuads.size()];
        int mandatoryIndex = 0;

        var optional = new ArrayList<Entry<Integer, byte[]>>(canonized.size() - mandatory.length);
        var baseWriter = new StringWriter(mandatory.length * 256);

        for (var nquad : canonized) {
            if (selectedNQuads.contains(nquad)) {
                selectedNQuads.remove(nquad);
                mandatory[mandatoryIndex++] = index;
                baseWriter.write(nquad);
            } else {
                optional.add(Map.entry(index, nquad.getBytes(StandardCharsets.UTF_8)));
            }
            index++;
        }

        if (!selectedNQuads.isEmpty()) {
            throw new IllegalArgumentException();
        }

        var base = new BasePayload();
        base.base = baseWriter.toString().getBytes(StandardCharsets.UTF_8);
        base.redactable = optional;
        base.pointers = mandatoryPointers;
        base.hmacKey = hmacKey;
//        IO.println("c14n > " + canonized);
//        IO.println("mandatory > " + Arrays.toString(mandatory));
//        IO.println("labels > " + canonizer.labels());
//        IO.println("mapping > " + hmac.mapping);
//        IO.println("base > " + new String(base.base));

        return base;
    }

    @Override
    public DigestiblePayload digestible() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void withProofs(Collection<String> ids) {
        // TODO Auto-generated method stub

    }

    private void lazyInit() {

        if (expandedDocument != null || dataset != null) {
            return;
        }

        var expanded = model.expand().apply(document);
//        IO.println("expanded > " + expanded);

        Object expandedProofs = null;

        if (expanded.size() != 1) {
            throw new IllegalArgumentException();
        }

        if (expanded.iterator().next() instanceof Map map) {
            if (map.containsKey("https://w3id.org/security#proof")) {

                expandedDocument = new HashMap<String, Object>(map);
//FIXME?!                expandedProofs = expandedDocument.remove("https://w3id.org/security#proof");
                expandedProofs = Map.of("https://w3id.org/security#proof",
                        expandedDocument.remove("https://w3id.org/security#proof"));
            } else {
                expandedDocument = new HashMap<String, Object>(map);
            }
        }

        if (expandedProofs != null) {
            dataset = new Dataset();

            model.tordf().accept(expandedProofs, dataset);
        }
//        IO.println(dataset.graphs);
//        IO.println(dataset.proofGraphs);

    }

    private static class Dataset implements QuadConsumer {

        private final Map<String, String> proofTypes = new HashMap<>();

        private Map<String, Collection<String[]>> graphs = new HashMap<>();

        private Collection<String> proofGraphs = new HashSet<>();

        @Override
        public void accept(
                String subject,
                String predicate,
                String object,
                String datatype,
                String language,
                String direction,
                String graph) {

            var key = graph;

            if (key == null) {
                key = "@default";

                if ("https://w3id.org/security#proof".equals(predicate)) {
                    proofGraphs.add(object);
                }

            } else if ("http://www.w3.org/1999/02/22-rdf-syntax-ns#type".equals(predicate)) {
                proofTypes.put(graph, object);
            }
//            IO.println(graph);
            graphs.computeIfAbsent(key, (_) -> new ArrayList<String[]>())
                    .add(new String[] {
                            subject, predicate, object, datatype, language, direction, graph
                    });
        }
    }
    

    private static class BasePayload implements RedactablePayload, PayloadWithHMAC {

        byte[] base;
        Collection<Entry<Integer, byte[]>> redactable;
        Collection<String> pointers;
        byte[] hmacKey;
        
        @Override
        public byte[] canonicalPayload() {
            return base;
        }

        @Override
        public void digest(String algorithm, byte[] value) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public byte[] digest(String algorithm) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Collection<String> digestAlgorithms() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Collection<Entry<Integer, byte[]>> redactablePayload() {
            return redactable;
        }

        @Override
        public Collection<String> pointers() {
            return pointers;
        }

        @Override
        public byte[] hmacKey() {
            return hmacKey;
        }

    }

}
