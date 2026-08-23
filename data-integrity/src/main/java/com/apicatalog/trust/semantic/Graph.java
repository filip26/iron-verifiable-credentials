package com.apicatalog.trust.semantic;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.Set;

import com.apicatalog.trust.LangString;

public record Graph(
        String id,
        SequencedMap<String, Node> nodes) {

    public static final String PREDICATE_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

    public static final class Node {

        final String id;
        final Graph graph;

        final SequencedSet<String> type;
        Collection<Statement> statements;

        public Node(
                String id,
                Graph graph) {
            this.id = id;
            this.graph = graph;
            this.type = new LinkedHashSet<String>(4);
            this.statements = List.of();
        }

        public void addStatement(String predicate, String object, String datatype, String language, String direction) {

            if (PREDICATE_TYPE.equals(predicate)) {
                type.add(object);
            }

            if (statements.isEmpty()) {
                statements = new ArrayList<>();
            }

            Statement statement = null;

            if (datatype == null) {
                statement = new ResourceStatement(predicate, object);

            } else if (language == null && direction == null) {
                statement = new LiteralStatement(predicate, object, datatype);

            } else {
                statement = new LangStringStatement(predicate, object, datatype, language, direction);
            }

            statements.add(statement);
        }

        public String id() {
            return id;
        }

        public Graph graph() {
            return graph;
        }

        public SequencedSet<String> type() {
            return type;
        }

        public Collection<Statement> statements() {
            return statements;
        }
    }

    public interface Statement {

        String predicate();

        String object();

        default String datatype() {
            return null;
        }

        default String language() {
            return null;
        }

        default String direction() {
            return null;
        }
    }

    public static record ResourceStatement(
            String predicate,
            String object) implements Statement {
    };

    public static record LiteralStatement(
            String predicate,
            String object,
            String datatype) implements Statement {
    };

    public static record LangStringStatement(
            String predicate,
            String object,
            String datatype,
            String language,
            String direction) implements Statement {

        public String tag() {

            String tag = "";

            if (language != null) {
                tag = language;
            }

            if (direction != null) {
                tag = tag + "_" + direction;
            }

            return tag;
        }
    };

    public static final SequencedCollection<LangString> langString(
            Graph.Statement statement,
            SequencedCollection<LangString> value,
            boolean allowAlts) {

        final LangString langString;

        if (statement instanceof LangStringStatement ls) {

            langString = new LangString(ls.object, ls.language, ls.direction);

        } else if (statement instanceof LiteralStatement literal) {

            // FIXME, check if string
            langString = new LangString(literal.object, null, null);

        } else {
            throw new IllegalArgumentException(" ..., but was " + statement);
        }

        if (value == null) {
            return List.of(langString);
        }

        var mutable = value;

        if (value.size() == 1) {
            mutable = new ArrayList<>(48);
            mutable.add(value.getFirst());
        }

        mutable.add(langString);
        return mutable;
    }

    public static final Instant xsdDateTime(Graph.Statement statement) {

        if (!(statement instanceof LiteralStatement literal)) {
            throw new IllegalArgumentException();
        }

        if (!"http://www.w3.org/2001/XMLSchema#dateTime".equals(literal.datatype())) {
            throw new IllegalArgumentException();
        }

        return Instant.parse(literal.object());
    }

    public static final Collection<?> nodes(
            SequencedCollection<?> context,
            Graph.Statement statement,
            Collection<?> value,
            Graph graph,
            Map<String, Graph> dataset,
            SemanticModel model,
            NodeMapping typeMapping) {

        if (value == null) {
            return List.of(node(context, statement, graph, dataset, model, typeMapping));
        }

        @SuppressWarnings("unchecked")
        var mutable = (Collection<Object>) value;

        if (value.size() == 1) {
            mutable = new ArrayList<>(value);
        }

        mutable.add(node(context, statement, graph, dataset, model, typeMapping));
        return mutable;
    }

    public static final Object node(
            SequencedCollection<?> context,
            Graph.Statement statement,
            Graph graph,
            Map<String, Graph> dataset,
            SemanticModel model,
            NodeMapping typeMapping) {

        if (!(statement instanceof ResourceStatement resource)) {
            throw new IllegalArgumentException();
        }

        var node = graph.nodes().get(resource.object());

        if (node != null) {

            if (typeMapping != null) {

                var mapper = typeMapping.mapper(statement.predicate(), node.type());

                if (mapper != null) {
                    return mapper.materialize(context, node, dataset, model);
                }
            }
            return node;
        }
        return resource.object();
    }

    public static final Set<String> resources(
            Graph.Statement statement,
            Set<String> value) {

        if (!(statement instanceof ResourceStatement)) {
            throw new IllegalArgumentException(" ..., but was " + statement);

        }

        var string = statement.object();

        if (value == null) {
            return Set.of(string);
        }

        var mutable = value;

        if (value.size() == 1) {
            mutable = new HashSet<>(value);
        }

        mutable.add(string);
        return mutable;
    }

    @FunctionalInterface
    public interface NodeMapper<T> {

        // reads from n-quads
        T materialize(
                SequencedCollection<?> context,
                Graph.Node node,
                Map<String, Graph> dataset,
                SemanticModel model);
    }

    @FunctionalInterface
    public interface NodeMapping {

        <T> NodeMapper<T> mapper(String predicate, Collection<String> types);

    }

    public record TypeMapping(String[] types, NodeMapper<?> mapper) {
    }

    public static final class TypeMappingMatcher {

        private static final Comparator<TypeMapping> BY_SIZE_DESC = Comparator
                .comparingInt((TypeMapping mapping) -> mapping.types().length)
                .reversed();

        private final TypeMapping[] mappings;

        public TypeMappingMatcher(Collection<TypeMapping> mappings) {
            this.mappings = mappings.toArray(TypeMapping[]::new);

            for (var mapping : this.mappings) {
                Arrays.sort(mapping.types());
            }

            Arrays.sort(this.mappings, BY_SIZE_DESC);
        }

        TypeMapping findBest(Set<String> types) {
            var query = types.toArray(String[]::new);
            Arrays.sort(query);

            TypeMapping best = null;
            int bestMatches = 0;

            for (var mapping : mappings) {
                if (mapping.types().length <= bestMatches) {
                    break;
                }

                int matches = intersectionSize(query, mapping.types());

                if (matches > bestMatches) {
                    bestMatches = matches;
                    best = mapping;

                    if (bestMatches == query.length) {
                        break;
                    }
                }
            }

            return best;
        }

        static int intersectionSize(String[] a, String[] b) {
            int ai = 0;
            int bi = 0;
            int matches = 0;

            while (ai < a.length && bi < b.length) {
                int cmp = a[ai].compareTo(b[bi]);

                if (cmp < 0) {
                    ai++;
                } else if (cmp > 0) {
                    bi++;
                } else {
                    matches++;
                    ai++;
                    bi++;
                }
            }

            return matches;
        }
    }
}
