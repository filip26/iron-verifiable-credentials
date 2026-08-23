package com.apicatalog.vcdm.v2;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.Set;

import com.apicatalog.trust.Document;
import com.apicatalog.trust.semantic.Graph;
import com.apicatalog.trust.semantic.Graph.ResourceStatement;
import com.apicatalog.trust.semantic.Graph.NodeMapping;
import com.apicatalog.trust.semantic.GraphAccessor;
import com.apicatalog.trust.semantic.SemanticModel;

public class Presentation {

    public static final String TYPE_URI = "https://www.w3.org/2018/credentials#VerifiablePresentation";
    public static final String TYPE_NAME = "VerifiablePresentation";

    public static final String PREDICATE_CREDENTIAL = "https://www.w3.org/2018/credentials#verifiableCredential";
    public static final String PREDICATE_HOLDER = "https://www.w3.org/2018/credentials#holder";
    public static final String PREDICATE_TERMS_OF_USE = "https://www.w3.org/2018/credentials#termsOfUse";

    public static final String PREDICATE_PROOF = "https://w3id.org/security#proof";
    
    public interface CredentialCursor {

        boolean next();

        Document.Accessor newAccessor();
    }

    private SequencedCollection<?> context;

    private URI id;
    private Set<String> type;
    private Object holder;
    private Collection<?> termsOfUse;

    private SemanticModel model;    //TODO use resolver to get model
    private Collection<Map<String, Graph>> credential;

    /**
     * Checks whether all mandatory properties of the presentation are present,
     * excluding the {@code verifiableCredential predicate.
     *
     * @return {@code true} if all required properties are present, {@code false}
     * otherwise
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
        return new CredentialCursor() {

            Iterator<Map<String, Graph>> datasets = credential.iterator();
            Map<String, Graph> currentDataset = null;

            @Override
            public boolean next() {
                if (datasets.hasNext()) {
                    this.currentDataset = datasets.next();
                    return true;
                }
                return false;
            }

            @Override
            public GraphAccessor newAccessor() {
                return GraphAccessor.newInstance(model, currentDataset);
            }
        };
    }

    public static class GraphMapper implements Graph.NodeMapper<Presentation> {

        private final NodeMapping typeMapping;

        public GraphMapper() {
            this(null);
        }

        public GraphMapper(NodeMapping typeMapping) {
            this.typeMapping = typeMapping;
        }

        @Override
        public Presentation materialize(
                SequencedCollection<?> context,
                Graph.Node node,
                Map<String, Graph> dataset,
                SemanticModel model) {

            var presentation = new Presentation();
            presentation.context = context;
            presentation.model = model;

            if (!node.id().startsWith("_:")) {
                presentation.id = URI.create(node.id());
            }

            for (var statement : node.statements()) {

                switch (statement.predicate()) {
                case Graph.PREDICATE_TYPE:
                    presentation.type = Graph.resources(statement, presentation.type);
                    break;

                case PREDICATE_HOLDER:
                    if (presentation.holder != null) {
                        throw new IllegalArgumentException();
                    }
                    presentation.holder = Graph.node(context, statement, node.graph(), dataset, model, typeMapping);
                    break;

                case PREDICATE_CREDENTIAL:
                    if (!(statement instanceof ResourceStatement)) {
                        throw new IllegalArgumentException();
                    }
                    if (presentation.credential == null) {
                        presentation.credential = new ArrayList<>();
                    }

                    var credential = new HashMap<>(dataset);
                    credential.put("@default", credential.remove(statement.object()));
                    
                    presentation.credential.add(credential);
                    break;

                case PREDICATE_TERMS_OF_USE:
                    presentation.termsOfUse = Graph.nodes(
                            context,
                            statement,
                            presentation.termsOfUse,
                            node.graph(),
                            dataset,
                            model,
                            typeMapping);
                    break;

                case PREDICATE_PROOF:
                    // ignored, not mapped directly
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
