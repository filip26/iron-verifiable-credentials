package com.apicatalog.di.sd.legacy;

class BaseDocument {

//    final DocumentLoader loader;
//
//    JsonObject skolemizedCompactDocument;
//
//    Collection<RdfNQuad> nquads;
//
//    Map<RdfResource, RdfResource> labelMap;
//
//    protected BaseDocument(DocumentLoader loader) {
//        this.loader = loader;
//    }
//
//    public static BaseDocument of(VerifiableMaterial unsignedData, DocumentLoader loader, HmacIdProvider hmac) throws DocumentError {
//
//        JsonStructure context = JsonUtils.toJsonArray(unsignedData.context().json());
//        JsonObject expanded = unsignedData.expanded();
//        
//        final BaseDocument cdoc = new BaseDocument(loader);
//
//        final JsonArray skolemizedExpandedDocument = Skolemizer.skolemize(Json.createArrayBuilder().add(expanded).build());
//
//        cdoc.skolemizedCompactDocument = Skolemizer.compact(skolemizedExpandedDocument, context, loader);
//
//        final Collection<RdfNQuad> deskolemizedNQuads = Skolemizer.deskolemize(skolemizedExpandedDocument, loader);
//
//        final RdfCanonicalizer canonicalizer = RdfCanonicalizer.newInstance(deskolemizedNQuads);
//
//        final Collection<RdfNQuad> dataset = canonicalizer.canonicalize();
//
//        final List<RdfNQuad> canonicalNQuads = new ArrayList<>(dataset.size());
//
//        for (RdfNQuad nquad : dataset) {
//            RdfResource subject = nquad.getSubject();
//            RdfValue object = nquad.getObject();
//
//            boolean clone = false;
//
//            if (subject.isBlankNode()) {
//                subject = hmac.getHmacId(subject);
//                clone = true;
//            }
//            if (object.isBlankNode()) {
//                object = hmac.getHmacId((RdfResource) object);
//                clone = true;
//            }
//
//            if (clone) {
//                canonicalNQuads.add(Rdf.createNQuad(subject, nquad.getPredicate(), object, nquad.getGraphName().orElse(null)));
//            } else {
//                canonicalNQuads.add(nquad);
//            }
//        }
//
//        Collections.sort(canonicalNQuads, RdfNQuadComparator.asc());
//
//        cdoc.nquads = Collections.unmodifiableList(canonicalNQuads);
//        
//        cdoc.labelMap = canonicalizer.issuer().mappingTable()
//                .entrySet().stream()
//                .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), hmac.mapping().get(e.getValue())))
//                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//
//        return cdoc;
//    }
//
//    public Map<Integer, RdfNQuad> select(
//            DocumentSelector selector) throws DocumentError {
//
//        Collection<RdfNQuad> selected = relabelBlankNodes(
//                Skolemizer.deskolemize(
//                        Json.createObjectBuilder(selector.getNodes(skolemizedCompactDocument))
//                                .add(Keywords.CONTEXT, skolemizedCompactDocument.get(Keywords.CONTEXT)).build(),
//                        loader),
//                labelMap);
//
//        Map<Integer, RdfNQuad> matching = new HashMap<>();
//
//        int index = 0;
//        for (final RdfNQuad nquad : nquads) {
//            if (selected.contains(nquad)) {
//                matching.put(index, nquad);
//            }
//            index++;
//        }
//        return matching;
//    }
//
//    protected static List<RdfNQuad> relabelBlankNodes(Collection<RdfNQuad> nquads, Map<RdfResource, RdfResource> labelMap) {
//
//        final List<RdfNQuad> relabeledNQuads = new ArrayList<>(nquads.size());
//
//        for (final RdfNQuad nquad : nquads) {
//
//            RdfResource subject = nquad.getSubject();
//            RdfValue object = nquad.getObject();
//
//            boolean clone = false;
//
//            if (subject.isBlankNode() && labelMap.containsKey(subject)) {
//                subject = labelMap.get(subject);
//                clone = true;
//            }
//            if (object.isBlankNode() && labelMap.containsKey(object)) {
//                object = labelMap.get(object);
//                clone = true;
//            }
//
//            relabeledNQuads.add(clone
//                    ? Rdf.createNQuad(subject, nquad.getPredicate(), object, nquad.getGraphName().orElse(null))
//                    : nquad);
//        }
//
//        return relabeledNQuads;
//    }
//
//    public Collection<RdfNQuad> nquads() {
//        return nquads;
//    }
//
//    public Map<RdfResource, RdfResource> labelMap() {
//        return labelMap;
//    }
}
