package com.apicatalog.trust.semantic;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedCollection;

public record Graph(
        String id,
        Map<String, Node> nodes) {

    public static final String PREDICATE_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

    public static final class Node {

        final String id;
        final String graph;

        SequencedCollection<String> type;
        Collection<Statement> statements;

        public Node(
                String id,
                String graph) {
            this.id = id;
            this.graph = graph;
            this.type = List.of();
            this.statements = List.of();
        }

        public void addStatement(String predicate, String object, String datatype, String language, String direction) {

            if (PREDICATE_TYPE.equals(predicate)) {
                if (type.isEmpty()) {
                    type = List.of(object);

                } else if (type.size() == 1) {
                    type = List.of(type.getFirst(), object);

                } else {
                    type = new ArrayList<>(type);
                    type.add(object);
                }
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

        public String graph() {
            return graph;
        }

        public SequencedCollection<String> type() {
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

    public static final Map<String, String> langMap(Graph.Statement statement, Map<String, String> value) {

        if (!(statement instanceof LangStringStatement langString)) {
            throw new IllegalArgumentException();
        }

        if (value == null) {
            return Map.of(langString.tag(), langString.object());

        }

        var mutable = value;

        if (value.size() == 1) {
            mutable = new HashMap<String, String>(value);
        }

        mutable.put(langString.tag(), langString.object());
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
    
    public static final Object resource(
            Graph.Statement statement, 
            Graph graph,
            SemanticModel model,
            Class<?> baseclazz, 
            TypeMapping typeMapping) {
        if (!(statement instanceof ResourceStatement resource)) {
            throw new IllegalArgumentException();
        }
        
        if (graph.nodes().containsKey(resource.object())) {

            var node = graph.nodes().get(resource.object());

            if (typeMapping != null) {

                var mapper = typeMapping.mapper(baseclazz, node.type());

                if (mapper != null) {
                    return mapper.materialize(node, graph, model);
                }
            }
            return node;
        }
        return resource.object();
    }

    @FunctionalInterface
    public interface NodeMapper<T> {

        // reads from n-quads
        T materialize(
                Graph.Node node,
                Graph graph,
                SemanticModel model);
    }

    @FunctionalInterface
    public interface TypeMapping {

        <T> NodeMapper<T> mapper(Class<T> baseclass, Collection<String> types);

    }
}
