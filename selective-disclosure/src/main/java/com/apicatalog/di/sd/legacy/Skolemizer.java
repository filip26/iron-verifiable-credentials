package com.apicatalog.di.sd.legacy;

public class Skolemizer {

    static final String URN_PREFIX = "urn:xyz:";

    final String urnScheme;
    final String prefix;
    int counter;

    private Skolemizer(final String urnScheme, final String random) {
        this.counter = 0;
        this.urnScheme = urnScheme;
        this.prefix = urnScheme + random + "_";
    }

//    public static JsonArray skolemize(JsonArray expanded) {
//        return (new Skolemizer(URN_PREFIX, Long.toHexString((long) (Math.random() * 100000))))
//                .skolemizeExpanded(expanded);
//    }
//
////    static JsonObject compact(JsonArray document, JsonStructure context, Function) {
////        try {
////            return JsonLd
////                    .compact(JsonDocument.of(document),
////                            JsonDocument.of(context))
////                    .loader(loader)
////                    .get().asJsonObject();
////        } catch (JsonLdError e) {
////            throw new IllegalArgumentException(e);
//    //// throw new DocumentError(e, ErrorType.Invalid);
////        }
////    }
//
//    public static Collection<RdfNQuad> deskolemize(
//            JsonStructure skolemizedDocument) {
////    static Collection<RdfNQuad> deskolemize(RdfDataset skolemizedDataset) {
//        try {
//            IO.println("1:   " + skolemizedDocument);
//            RdfDataset skolemizedDataset = JsonLd.toRdf(JsonDocument.of(skolemizedDocument)).get();
//
////        var skolemizedDataset = tordf.accept(skolemizedDocument);
//
//            Collection<RdfNQuad> skolemizedNQuads = skolemizedDataset.toList();
//            IO.println("2:   " + skolemizedNQuads);
//            return deskolemize(skolemizedNQuads, Skolemizer.URN_PREFIX);
//        } catch (JsonLdError e) {
//            //// throw new DocumentError(e, ErrorType.Invalid);
////            throw new IllegalArgumentException(e);
//        }
//        return null;
//    }
//
//    private static Collection<RdfNQuad> deskolemize(Collection<RdfNQuad> skolemizedNQuads, String urnScheme) {
//
//        final Collection<RdfNQuad> deskolemizedNQuads = new ArrayList<>(skolemizedNQuads.size());
//
//        for (RdfNQuad skolemized : skolemizedNQuads) {
//            RdfResource subject = skolemized.getSubject();
//            RdfValue object = skolemized.getObject();
//
//            boolean clone = false;
//
//            if (subject.isIRI() && subject.getValue().startsWith(urnScheme)) {
//                subject = Rdf.createBlankNode(subject.getValue().substring(urnScheme.length()));
//                clone = true;
//            }
//            if (object.isIRI() && object.getValue().startsWith(urnScheme)) {
//                object = Rdf.createBlankNode(object.getValue().substring(urnScheme.length()));
//                clone = true;
//            }
//
//            if (clone) {
//                deskolemizedNQuads.add(Rdf.createNQuad(subject, skolemized.getPredicate(), object,
//                        skolemized.getGraphName().orElse(null)));
//            } else {
//                deskolemizedNQuads.add(skolemized);
//            }
//        }
//
//        return deskolemizedNQuads;
//    }
//
//    private JsonArray skolemizeExpanded(final JsonArray expanded) {
//
//        final JsonArrayBuilder builder = Json.createArrayBuilder();
//
//        for (final JsonValue item : expanded) {
//            if (JsonUtils.isNotObject(item) || ValueObject.isValueObject(item)) {
//                builder.add(item);
//                continue;
//            }
//
//            final JsonObjectBuilder node = Json.createObjectBuilder();
//
//            boolean idFound = false;
//
//            for (final Map.Entry<String, JsonValue> entry : item.asJsonObject().entrySet()) {
//
//                final String key = entry.getKey();
//                final JsonValue value = entry.getValue();
//
//                if (Keywords.ID.equals(key)) {
//                    final String id = item.asJsonObject().getString(Keywords.ID);
//
//                    node.add(Keywords.ID, BlankNode.hasPrefix(id)
//                            ? Json.createValue(urnScheme + id.substring(2))
//                            : item.asJsonObject().get(Keywords.ID));
//                    idFound = true;
//
//                } else if (JsonUtils.isArray(value)) {
//                    node.add(key, skolemizeExpanded(value.asJsonArray()));
//
//                } else {
//                    node.add(key, skolemizeExpanded(Json.createArrayBuilder().add(value).build()).get(0));
//                }
//            }
//
//            if (!idFound) {
//                node.add(Keywords.ID, Json.createValue(prefix + (counter++)));
//            }
//
//            builder.add(node);
//        }
//        
//        return builder.build();
//    }
}
