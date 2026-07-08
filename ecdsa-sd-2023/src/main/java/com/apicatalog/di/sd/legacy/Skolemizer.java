package com.apicatalog.di.sd.legacy;

class Skolemizer {

    static final String URN_PREFIX = "urn:xyz:";
//
//    final String urnScheme;
//    final String random;
//    int counter;
//
//    private Skolemizer(final String urnScheme, final String random) {
//        this.counter = 0;
//        this.urnScheme = urnScheme;
//        this.random = urnScheme + random + "_";
//    }
//
//    static JsonArray skolemize(JsonArray expanded) throws DocumentError {
//        return (new Skolemizer(URN_PREFIX, Long.toHexString((long) (Math.random() * 100000)))).skolemizeExpanded(expanded);
//    }
//
//    static JsonObject compact(JsonArray document, JsonStructure context, final DocumentLoader loader) throws DocumentError {
//        try {
//            return JsonLd
//                    .compact(JsonDocument.of(document),
//                            JsonDocument.of(context))
//                    .loader(loader)
//                    .get().asJsonObject();
//        } catch (JsonLdError e) {
//            throw new DocumentError(e, ErrorType.Invalid);
//        }
//    }
//
//    static Collection<RdfNQuad> deskolemize(JsonStructure skolemizedDocument, final DocumentLoader loader) throws DocumentError {
//
//        try {
//            RdfDataset skolemizedDataset = JsonLd.toRdf(JsonDocument.of(skolemizedDocument)).loader(loader).get();
//
//            Collection<RdfNQuad> skolemizedNQuads = skolemizedDataset.toList();
//
//            return deskolemize(skolemizedNQuads, Skolemizer.URN_PREFIX);
//        } catch (JsonLdError e) {
//            throw new DocumentError(e, ErrorType.Invalid);
//        }
//
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
//                deskolemizedNQuads.add(Rdf.createNQuad(subject, skolemized.getPredicate(), object, skolemized.getGraphName().orElse(null)));
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
//                node.add(Keywords.ID, Json.createValue(random + (counter++)));
//            }
//
//            builder.add(node);
//        }
//        return builder.build();
//    }
}
