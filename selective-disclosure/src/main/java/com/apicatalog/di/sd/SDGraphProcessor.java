package com.apicatalog.di.sd;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.apicatalog.multibase.Multibase;
import com.apicatalog.trust.model.SemanticModel;
import com.apicatalog.trust.model.SemanticModel.QuadConsumer;
import com.apicatalog.trust.payload.DigestiblePayload;
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

    public SDBaseDocument redactable(Collection<String> mandatoryPointers, byte[] hmacKey) {

        lazyInit();

        var skolemized = Skolemizer.skolemize(expandedDocument);

        var compacted = model.compact().apply(context, skolemized);

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
        var hmac = Hmac.newInstance(hmacKey);

        canonizer.canonize((subject, predicate, object, datatype, language, direction, graph) -> {

            var s = subject;
            if (s.startsWith("_:")) {
                s = hmac.assignId(s);
            }
            var o = object;
            if (o.startsWith("_:")) {
                o = hmac.assignId(o);
            }

            canonized.add(canonizer.toNQuad(s, predicate, o, datatype, language, direction, graph));
        });

        Collections.sort(canonized);

        var selection = Pointer.select(compacted, mandatoryPointers);

        var mandatoryNQuads = new HashSet<String>(selection.size());

        model.tordf().accept(selection, ((subject, predicate, object, datatype, language, direction, graph) -> {

            var s = subject;
            if (s.startsWith(Skolemizer.URN_PREFIX)) {
                s = hmac.getId(canonizer.labels().get("_:" + s.substring(Skolemizer.URN_PREFIX.length())));
            }
            var o = object;
            if (o.startsWith(Skolemizer.URN_PREFIX)) {
                o = hmac.getId(canonizer.labels().get("_:" + o.substring(Skolemizer.URN_PREFIX.length())));
            }

            var nquad = canonizer.toNQuad(s, predicate, o, datatype, language, direction, graph);

            mandatoryNQuads.add(nquad);
        }));

        var labels = HashMap.<String, String>newHashMap(selection.size());
        for (var label : canonizer.labels().entrySet()) {
            labels.put(label.getKey(), hmac.mapping().get(label.getValue()));
        }

        int index = 0;

        var mandatoryIndices = new int[mandatoryNQuads.size()];
        int mandatoryIndex = 0;

        var optionalIndices = new int[canonized.size() - mandatoryIndices.length];
        int optionalIndex = 0;

        var optional = new byte[canonized.size() - mandatoryIndices.length][];

        var baseWriter = new StringWriter(mandatoryIndices.length * 256);

        for (var nquad : canonized) {
            if (mandatoryNQuads.contains(nquad)) {
                mandatoryNQuads.remove(nquad);
                mandatoryIndices[mandatoryIndex++] = index;
                baseWriter.write(nquad);
            } else {
                optionalIndices[optionalIndex] = index;
                optional[optionalIndex++] = nquad.getBytes(StandardCharsets.UTF_8);
            }
            index++;
        }

        if (!mandatoryNQuads.isEmpty()) {
            throw new IllegalArgumentException();
        }

        var base = new SDBaseDocument();
        base.base = baseWriter.toString().getBytes(StandardCharsets.UTF_8);
        base.redactable = optional;
        base.redactableIndices = optionalIndices;
        base.mandatoryPointers = mandatoryPointers;
        base.mandatoryIndices = mandatoryIndices;
        base.hmacKey = hmacKey;

        base.labels = labels;
        base.compacted = compacted;
        base.canonized = canonized;
        base.model = model;

        return base;
    }

    public SDDerivedDocument derived(Map<Integer, byte[]> labels, int[] indices) {
        lazyInit();

        var expanded = expandedDocument;

        var canonizer = model.newCanonizer();

        var consumer = canonizer.consumer();

        model.tordf().accept(expanded, consumer);

        var canonized = new ArrayList<String[]>();

        canonizer.canonize(((subject, predicate, object, datatype, language, direction, graph) -> {
            canonized.add(
                    new String[] {
                            subject, predicate, object, datatype, language, direction, graph
                    });

        }));

        var labelMap = canonizer.labels().values().stream()
                .sorted()
                .toList();

        var map = HashMap.<String, String>newHashMap(labels.size());

        for (int i = 0; i < labelMap.size(); i++) {
            map.put(labelMap.get(i), "_:" + Multibase.BASE_64_URL.encode(labels.get(i)));
        }

        var nquads = new ArrayList<String>(canonized.size());

        // relabel blank nodes
        for (var quad : canonized) {
            if (quad[0].startsWith("_:") && map.containsKey(quad[0])) {
                quad[0] = map.get(quad[0]);
            }
            if (quad[2].startsWith("_:") && map.containsKey(quad[2])) {
                quad[2] = map.get(quad[2]);
            }
            var nquad = canonizer.toNQuad(quad[0], quad[1], quad[2], quad[3], quad[4], quad[5], quad[6]);
            nquads.add(nquad);
        }

        Collections.sort(nquads);

        var mandatory = new StringWriter(indices.length * 256);
        var disclosed = new byte[nquads.size() - indices.length][];

        Arrays.sort(indices);

        var combinedIndices = new int[nquads.size() - indices.length];

        int index = 0;
        int disclosedIndex = 0;

        for (var nquad : nquads) {

            if (Arrays.binarySearch(indices, index) >= 0) {
                mandatory.write(nquad);

            } else {
                combinedIndices[disclosedIndex] = index;
                disclosed[disclosedIndex++] = nquad.getBytes(StandardCharsets.UTF_8);
            }

            index++;
        }

        return new SDDerivedDocument(
                mandatory.toString().getBytes(StandardCharsets.UTF_8),
                disclosed,
                indices,
                labels);
    }

    @Override
    public DigestiblePayload digestible() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Yet, not supported.");
    }

    @Override
    public void withProofs(Collection<String> ids) {
        // TODO Auto-generated method stub
        return;
    }

    private void lazyInit() {

        if (expandedDocument != null || dataset != null) {
            return;
        }

        var expanded = model.expand().apply(document);

        if (expanded.size() != 1) {
            throw new IllegalArgumentException();
        }

        Object expandedProofs = null;

        if (expanded.iterator().next() instanceof Map map) {
            expandedDocument = new HashMap<String, Object>(map);
            if (map.containsKey("https://w3id.org/security#proof")) {
                expandedProofs = Map.of("https://w3id.org/security#proof",
                        expandedDocument.remove("https://w3id.org/security#proof"));
            }
        }

        if (expandedProofs != null) {
            dataset = new Dataset();

            model.tordf().accept(expandedProofs, dataset);
        }
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

            graphs.computeIfAbsent(key, (_) -> new ArrayList<String[]>())
                    .add(new String[] {
                            subject, predicate, object, datatype, language, direction, graph
                    });
        }
    }

    public static record SignatureAlgorithm(String signature, String digest) {
    };
}
