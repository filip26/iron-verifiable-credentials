package com.apicatalog.di.sd.legacy;

class Selection {

//    Map<Integer, RdfNQuad> matching;
//    Collection<RdfNQuad> deskolemizedNQuads;
//
//    public static Selection of(final BaseDocument base, final DocumentSelector selector) throws DocumentError {
//        return get(base, selector);
//    }
//
//    protected static Selection get(final BaseDocument base, final DocumentSelector selector) throws DocumentError {
//
//        final Selection result = new Selection();
//
//        final JsonObject selection = selector.getNodes(base.skolemizedCompactDocument);
//
//        result.deskolemizedNQuads = Skolemizer.deskolemize(
//                Json.createObjectBuilder(selection)
//                        .add(Keywords.CONTEXT, base.skolemizedCompactDocument.get(Keywords.CONTEXT)).build(),
//                base.loader);
//
//        final List<RdfNQuad> nquads = BaseDocument.relabelBlankNodes(result.deskolemizedNQuads, base.labelMap);
//
//        result.matching = new HashMap<>();
//
//        int index = 0;
//        for (final RdfNQuad nquad : base.nquads) {
//            if (nquads.contains(nquad)) {
//                result.matching.put(index, nquad);
//            }
//            index++;
//        }
//        return result;
//    }
}
