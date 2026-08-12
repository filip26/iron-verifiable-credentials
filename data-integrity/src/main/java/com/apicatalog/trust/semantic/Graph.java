package com.apicatalog.trust.semantic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SequencedCollection;

public record Graph(
        String id,
        Map<String, Node> nodes) {

    public static final String PREDICATE_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

    public static class Node {

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
    };
}
