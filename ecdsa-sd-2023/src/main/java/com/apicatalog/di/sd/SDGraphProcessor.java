package com.apicatalog.di.sd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.apicatalog.rdf.Rdf;
import com.apicatalog.rdf.RdfNQuad;
import com.apicatalog.rdf.RdfResource;
import com.apicatalog.rdf.RdfValue;
import com.apicatalog.rdf.api.RdfConsumerException;
import com.apicatalog.rdf.canon.RdfCanon;
import com.apicatalog.rdf.nquads.NQuadsWriter;
import com.apicatalog.trust.model.SemanticModel;
import com.apicatalog.trust.model.SemanticModel.QuadConsumer;
import com.apicatalog.trust.payload.DigestiblePayload;
import com.apicatalog.trust.payload.RedactablePayload;
import com.apicatalog.trust.processor.GraphProcessor;
import com.apicatalog.trust.signature.Signature;

import jakarta.json.JsonObject;

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
    public RedactablePayload redactable(Signature signature, Collection<String> mandatoryPointers) {

        lazyInit();

        var skolemized = Skolemizer.skolemize(expandedDocument);

        var compacted = model.compact().apply(context, skolemized);

        IO.println("compacted > " + compacted);

        var selection = Selection.select(compacted, mandatoryPointers);

        IO.println("selection > " + selection);

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

        //TODO read from model
        var hmac = HmacIdProvider.newInstance(((BaseProofValue)signature).hmacKey());
        
        canonizer.canonize((subject, predicate, object, datatype, language, direction, graph) -> {
            
            var s = subject;
            if (s.startsWith("_:")) {
                s = hmac.getHmacId(s.substring(2));
            }
            var o = object;
            if (o.startsWith("_:")) {
                o = hmac.getHmacId(o.substring(2));
            }
            
            canonized.add(NQuadsWriter.nquad(s, predicate, o, datatype, language, direction, graph));
            
        });

        var mandatory = new ArrayList<String>();
        model.tordf().accept(selection, ((subject, predicate, object, datatype, language, direction, graph) -> {
            mandatory.add(Skolemizer.deskolemize(subject, predicate, object, datatype, language, direction, graph));
        }));

        IO.println("c14n > " + canonized);
        IO.println("selection > " + mandatory);
        IO.println("labels > " + canonizer.labels());
        IO.println("mapping > " + hmac.mapping);

        return null;
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

        if (dataset != null) {
            return;
        }

        var expanded = model.expand().apply(document);
        IO.println("expanded > " + expanded);

        Object expandedProofs = null;

        if (expanded.size() != 1) {
            throw new IllegalArgumentException();
        }

        if (expanded.iterator().next() instanceof Map map
                && map.containsKey("https://w3id.org/security#proof")) {

            expandedDocument = new HashMap<String, Object>(map);
//                expandedProofs = expandedDocument.remove("https://w3id.org/security#proof");
            expandedProofs = Map.of("https://w3id.org/security#proof",
                    expandedDocument.remove("https://w3id.org/security#proof"));
        }

        if (expandedProofs == null || expandedDocument == null) {
            return;
        }

        dataset = new Dataset();

        model.tordf().accept(expandedProofs, dataset);

        IO.println(dataset.graphs);
        IO.println(dataset.proofGraphs);

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
            IO.println(graph);
            graphs.computeIfAbsent(key, (_) -> new ArrayList<String[]>())
                    .add(new String[] {
                            subject, predicate, object, datatype, language, direction, graph
                    });
        }
    }

    private BasePayload select(
            Collection<String> mandatoryPointers,
            Collection<String> context,
            Collection<String[]> graph) {
//    public BasePayload filter(Collection<String> mandatoryPointers) {
        IO.println("FILTER: " + mandatoryPointers + ", " + context + ", " + graph);

        final var canonizer = RdfCanon.create("SHA-256");
//        final var compactor = LegacyJsonLd.fromRDF();

        for (var quad : graph) {

            if ("https://w3id.org/security#proof".equals(quad[1])) {
                continue;
            }

            var subject = quad[0];
            if (subject.startsWith("_:")) {
//                subject = "urn:xyz:" + subject.substring(2);
            }
            var object = quad[2];
            if (object.startsWith("_:")) {
//                object = "urn:xyz:" + object.substring(2);
            }

//            compactor.accept(subject, quad[1], object, quad[3], quad[4], quad[5], null);

            canonizer.quad(quad[0], quad[1], quad[2], quad[3], quad[4], quad[5], null);
        }

//        var compacted = LegacyJsonLd.compacted(context);
//        IO.println(compacted);
//        var selection = Selection.select(compacted, mandatoryPointers);

        try {
            canonizer.provide((subject, predicate, object, datatype, language, direction, _) -> {

                IO.println(subject + " " + predicate + "  " + object);

                return null;
            });
        } catch (RdfConsumerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//        IO.println(selection);
        IO.println(canonizer.mapping());

//        JsonStructure context = JsonUtils.toJsonArray(unsignedData);
//      JsonObject expanded = unsignedData.expanded();
//      
//      final BaseDocument cdoc = new BaseDocument(loader);
//

//        var r = FragmentSelector.select(data, List.of(
//                "/type/0",
//                "/credentialSubject/type/1",
//                "/credentialSubject/employmentAuthorizationDocument/identifier",
//                "/credentialSubject/givenName",
//                "/issuer/id"
//
//        ));
//        IO.println(">>> " + r);

//        var bos = new ByteArrayOutputStream();
//        try (var emitter = JakartaEmitter.newEmitter(bos, Json.createGeneratorFactory(Map.of()))) {
//            Tree.write(r, emitter);
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        IO.println(new String(bos.toByteArray()));

//        var bos = new ByteArrayOutputStream();
//        try (var emitter = JakartaEmitter.newEmitter(bos, Json.createGeneratorFactory(Map.of()))) {
//            Tree.write(r, emitter);
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }

        try {

//            expanded = JsonLd.expand(JsonDocument.of(new ByteArrayInputStream(bos.toByteArray()))).get();
            //// IO.println("> " + expanded);
//            final JsonArray skolemizedExpandedDocument = Skolemizer.skolemize(expanded);
//            IO.println("> " + skolemizedExpandedDocument);
//
//            var xyz = Json.createArrayBuilder();
//            context.forEach(xyz::add);
//
//            skolemizedCompactDocument = JsonLd.compact(JsonDocument.of(skolemizedExpandedDocument),
//                    JsonDocument.of(xyz.build())).get();
//            IO.println("> " + skolemizedCompactDocument);
            //// compact.apply(skolemizedExpandedDocument, context);
//
//            final Collection<RdfNQuad> deskolemizedNQuads = Skolemizer.deskolemize(skolemizedExpandedDocument);
//            IO.println("> " + deskolemizedNQuads);
//
//            final Collection<RdfNQuad> dataset = deskolemizedNQuads;
            ////                    RdfCanonicalizer.canonicalize(deskolemizedNQuads);
//
//            final List<RdfNQuad> canonicalNQuads = new ArrayList<>(dataset.size());
//
//            var hmac = HmacIdProvider.newInstance("00112233445566778899AABBCCDDEEFF00112233445566778899AABBCCDDEEFF".getBytes());
//            
//            for (RdfNQuad nquad : dataset) {
//                RdfResource subject = nquad.getSubject();
//                RdfValue object = nquad.getObject();
//
//                boolean clone = false;
//
//                if (subject.isBlankNode()) {
//                    subject = hmac.getHmacId(subject);
//                    clone = true;
//                }
//                if (object.isBlankNode()) {
//                    object = hmac.getHmacId((RdfResource) object);
//                    clone = true;
//                }
//
//                if (clone) {
//                    canonicalNQuads.add(
//                            Rdf.createNQuad(subject, nquad.getPredicate(), object, nquad.getGraphName().orElse(null)));
//                } else {
//                    canonicalNQuads.add(nquad);
//                }
//            }
//
            //// Collections.sort(canonicalNQuads, RdfNQuadComparator.asc());
//            
//            IO.println("CQ > " + canonicalNQuads);
//
//            Map<RdfResource, RdfResource> labelMap = new HashMap<RdfResource, RdfResource>();
//            
//            for (var pointer : mandatoryPointers) {
//                IO.println(pointer);
//                IO.println(skolemizedCompactDocument.getValue(pointer));
//                
//                var x  = Json.createPointer(pointer).add(Json.createObjectBuilder().build(), 
//                        (JsonObject) skolemizedCompactDocument.getValue(pointer));
//
//                IO.println(">>> " + x);
//                
//                IO.println(select(
//                        x, 
//                        canonicalNQuads, 
//                        labelMap));   
//            }
//
//            
        } catch (Exception e) {
            e.printStackTrace();
        }
//        Tree.write(data, null);

//        final Collection<RdfNQuad> deskolemizedNQuads = Skolemizer.deskolemize(skolemizedExpandedDocument);
//
//    final RdfCanonicalizer canonicalizer = RdfCanonicalizer.newInstance(deskolemizedNQuads);
//
//    final Collection<RdfNQuad> dataset = canonicalizer.canonicalize();
//
//    final List<RdfNQuad> canonicalNQuads = new ArrayList<>(dataset.size());

        // TODO Auto-generated method stub
        return null;
    }

    public Map<Integer, RdfNQuad> select(
            JsonObject object,
            List<RdfNQuad> nquads,
            Map<RdfResource, RdfResource> labelMap) {

//        Collection<RdfNQuad> selected = relabelBlankNodes(
//                Skolemizer.deskolemize(
//                        Json.createObjectBuilder(
//                                object
        ////                                selector.getNodes(skolemizedCompactDocument)
//                        )
//                                .add("@context", skolemizedCompactDocument.get("@context"))
//                                .add("type", skolemizedCompactDocument.get("type")).build()),
//                labelMap);
//        IO.println("SQ: " + selected);
        Map<Integer, RdfNQuad> matching = new HashMap<>();

        int index = 0;
        for (final RdfNQuad nquad : nquads) {
//            if (selected.contains(nquad)) {
//                matching.put(index, nquad);
//            }
            index++;
        }
        return matching;
    }

    protected static List<RdfNQuad> relabelBlankNodes(Collection<RdfNQuad> nquads,
            Map<RdfResource, RdfResource> labelMap) {

        final List<RdfNQuad> relabeledNQuads = new ArrayList<>(nquads.size());

        for (final RdfNQuad nquad : nquads) {

            RdfResource subject = nquad.getSubject();
            RdfValue object = nquad.getObject();

            boolean clone = false;

            if (subject.isBlankNode() && labelMap.containsKey(subject)) {
                subject = labelMap.get(subject);
                clone = true;
            }
            if (object.isBlankNode() && labelMap.containsKey(object)) {
                object = labelMap.get(object);
                clone = true;
            }

            relabeledNQuads.add(clone
                    ? Rdf.createNQuad(subject, nquad.getPredicate(), object, nquad.getGraphName().orElse(null))
                    : nquad);
        }

        return relabeledNQuads;
    }

}
