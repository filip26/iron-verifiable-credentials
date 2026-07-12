package com.apicatalog.di.sd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.apicatalog.rdf.nquads.NQuadsWriter;

class Skolemizer {

    static final String URN_PREFIX = "urn:xyz:";

    private final String urnScheme;
    private final String prefix;
    private int counter;

    public Skolemizer(final String urnScheme, final String randomValue) {
        this.counter = 0;
        this.urnScheme = urnScheme;
        this.prefix = urnScheme + "s" + randomValue + "c";
    }

    public static Map<String, Object> skolemize(Map<String, Object> expanded) {
        return new Skolemizer(
                URN_PREFIX,
                Long.toHexString((long) (Math.random() * 10000000)))
                .skolemizeExpanded(expanded);
    }

    private Collection<Object> skolemizeExpanded(final Collection<?> collection) {

        var skolemized = new ArrayList<>(collection.size());

        for (var element : collection) {
            if (element instanceof Map map) {
                skolemized.add(skolemizeExpanded(map));

            } else {
                skolemized.add(element);
            }
        }

        return skolemized;
    }

    private Map<String, Object> skolemizeExpanded(final Map<String, Object> document) {

        if (document.containsKey("@value")) {
            return document;
        }

        var node = HashMap.<String, Object>newHashMap(document.size() + 1);

        boolean idFound = false;

        for (var entry : document.entrySet()) {

            var key = entry.getKey();
            var value = entry.getValue();

            if ("@id".equals(key)) {

                if (!(value instanceof String id)) {
                    throw new IllegalArgumentException();
                }

                node.put("@id", id.startsWith("_:")
                        ? urnScheme + id.substring(2)
                        : id);
                idFound = true;

            } else if (value instanceof Collection collection) {

                if ("@type".equals(key)) {
                    node.put(key, value);

                } else {
                    node.put(key, skolemizeExpanded(collection));
                }

            } else {
                throw new IllegalArgumentException("Not expanded JSON-LD");
            }
        }

        if (!idFound) {
            node.put("@id", prefix + (counter++));
        }
        return node;
    }
    
    public static String deskolemize(
            String subject, 
            String predicate, 
            String object, 
            String datatype, 
            String language, 
            String direction,
            String graph
            ) {
        
        var s = subject;
        if (s.startsWith(URN_PREFIX)) {
            s = "_:" + s.substring(URN_PREFIX.length());
        }
        var o = object;
        if (o.startsWith(URN_PREFIX)) {
            o = "_:" + o.substring(URN_PREFIX.length());
        }        
        return NQuadsWriter.nquad(s, predicate, o, datatype, language, direction, graph);
    }

}
