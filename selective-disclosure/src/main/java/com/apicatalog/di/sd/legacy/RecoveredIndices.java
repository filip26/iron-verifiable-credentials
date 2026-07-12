package com.apicatalog.di.sd.legacy;

class RecoveredIndices {

//    final DocumentLoader loader;
//
//    List<RdfNQuad> mandatory;
//    List<RdfNQuad> nonMandatory;
//
//    RecoveredIndices(DocumentLoader loader) {
//        this.loader = loader;
//    }
//    //JsonStructure context1,
//    static RecoveredIndices of(JsonObject expanded, DocumentLoader loader, Map<Integer, byte[]> labels, int[] indices) throws DocumentError {
//
//        try {
//            final Collection<RdfNQuad> dataset = JsonLd.toRdf(JsonDocument.of(expanded)).get().toList();
//
//            final RdfCanonicalizer canonicalizer = RdfCanonicalizer.newInstance(dataset);
//
//            final Collection<RdfNQuad> cdoc = canonicalizer.canonicalize();
//
//            List<RdfResource> labelMap = canonicalizer.issuer().mappingTable().entrySet()
//                    .stream().sorted(new Comparator<Entry<RdfResource, RdfResource>>() {
//
//                        @Override
//                        public int compare(Entry<RdfResource, RdfResource> o1, Entry<RdfResource, RdfResource> o2) {
//                            return o1.getValue().toString().compareTo(o2.getValue().toString());
//                        }
//
//                    }).map(Map.Entry::getValue).collect(Collectors.toList());
//
//            Map<RdfResource, RdfResource> map = new HashMap<>(labels.size());
//
//            for (int i = 0; i < labelMap.size(); i++) {
//                map.put(labelMap.get(i), Rdf.createBlankNode(Multibase.BASE_64_URL.encode(labels.get(i))));
//            }
//
//            final List<RdfNQuad> cquads = BaseDocument.relabelBlankNodes(cdoc, map);
//
//            Collections.sort(cquads, RdfNQuadComparator.asc());
//                        
//            RecoveredIndices ri = new RecoveredIndices(loader);
//
//            ri.mandatory = new ArrayList<>();
//            ri.nonMandatory = new ArrayList<>();
//
//            for (int i = 0; i < cquads.size(); i++) {
//                if (Arrays.contains(indices, i)) {
//                    ri.mandatory.add(cquads.get(i));
//                } else {
//                    ri.nonMandatory.add(cquads.get(i));
//                }
//            }
//
//            return ri;
//
//        } catch (JsonLdError e) {
//            throw new DocumentError(e, ErrorType.Invalid);
//        }
//    }

}
