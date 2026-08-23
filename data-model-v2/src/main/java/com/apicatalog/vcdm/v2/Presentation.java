package com.apicatalog.vcdm.v2;

import java.net.URI;
import java.util.Collection;
import java.util.SequencedCollection;
import java.util.Set;

import com.apicatalog.trust.Document;
import com.apicatalog.trust.semantic.Graph;
import com.apicatalog.trust.semantic.Graph.TypeMapping;
import com.apicatalog.trust.semantic.SemanticModel;

public class Presentation {

    public static final String TYPE_URI = "https://www.w3.org/2018/credentials#VerifiablePresentation";
    public static final String TYPE_NAME = "VerifiablePresentation";

    public static final String PREDICATE_CREDENTIAL = "https://www.w3.org/2018/credentials#verifiableCredential";
    public static final String PREDICATE_HOLDER = "https://www.w3.org/2018/credentials#holder";
    public static final String PREDICATE_TERMS_OF_USE = "https://www.w3.org/2018/credentials#termsOfUse";

    public interface CredentialCursor {

        boolean next();

        Document.Accessor newAccessor();

    }

    private SequencedCollection<?> context;
    
    private URI id;
    private Set<String> type;
    private Object holder;
    private Collection<?> termsOfUse;

    /**
     * Checks whether all mandatory properties of the presentation are present,
     * excluding the {@code verifiableCredential predicate.
     *
     * @return {@code true} if all required properties are present, {@code false}
     *         otherwise
     */
    public boolean hasRequired() {
        return type != null && type.contains(TYPE_URI);
    }
    
    /**
     * The JSON-LD context used to process the presentation.
     * 
     * @return a collection of strings representing the JSON-LD context URIs
     */
    public SequencedCollection<?> context() {
        return context;
    }

    public URI id() {
        return id;
    }

    public Set<String> type() {
        return type;
    }

    public Object holder() {
        return holder;
    }

    public Collection<?> termsOfUse() {
        return termsOfUse;
    }

    public CredentialCursor newCredentialCursor() {
        return null;
    }

    public static class GraphMapper implements Graph.NodeMapper<Presentation> {

        private final TypeMapping typeMapping;

        public GraphMapper() {
            this(null);
        }

        public GraphMapper(TypeMapping typeMapping) {
            this.typeMapping = typeMapping;
        }

        @Override
        public Presentation materialize(
                SequencedCollection<?> context,
                Graph.Node node,
                SemanticModel model) {

            var presentation = new Presentation();
            presentation.context = context;

            if (!node.id().startsWith("_:")) {
                presentation.id = URI.create(node.id());
            }

            for (var statement : node.statements()) {

                switch (statement.predicate()) {
                case Graph.PREDICATE_TYPE:
                    presentation.type = Graph.ids(statement, presentation.type);
                    break;
                    
                case PREDICATE_HOLDER:
                    if (presentation.holder != null) {
                        throw new IllegalArgumentException();
                    }
                    presentation.holder = Graph.resource(context, statement, node.graph(), model, typeMapping);
                    break;

                default:
                    throw new IllegalArgumentException(
                            """
                            Unrecognized predicate has been found %s.
                            """.formatted(statement.predicate()));
                }
            }

            return presentation;
        }
    }

}
