package com.apicatalog.vcdm.v2;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.SequencedCollection;

import com.apicatalog.trust.semantic.Graph;
import com.apicatalog.trust.semantic.Graph.TypeMapping;
import com.apicatalog.trust.semantic.SemanticModel;

public class Credential {

    public static final String TYPE_URI = "https://www.w3.org/2018/credentials#VerifiableCredential";
    public static final String TYPE_NAME = "VerifiableCredential";

    public static final String PREDICATE_NAME = "https://schema.org/name";
    public static final String PREDICATE_DESCRIPTION = "https://schema.org/description";
    public static final String PREDICATE_ISSUER = "https://www.w3.org/2018/credentials#issuer";
    public static final String PREDICATE_VALID_FROM = "https://www.w3.org/2018/credentials#validFrom";
    public static final String PREDICATE_VALID_UNTIL = "https://www.w3.org/2018/credentials#validUntil";

    public static final String PREDICATE_SUBJECT = "https://www.w3.org/2018/credentials#credentialSubject";
    public static final String PREDICATE_STATUS = "https://www.w3.org/2018/credentials#credentialStatus";
    public static final String PREDICATE_SCHEMA = "https://www.w3.org/2018/credentials#credentialSchema";
    public static final String PREDICATE_CONFIDENCE_METHOD = "https://www.w3.org/2018/credentials#credentialSchema";
    
    public static final String PREDICATE_EVIDENCE = "https://www.w3.org/2018/credentials#evidence";
    public static final String PREDICATE_REFRESH_SERVICE = "https://www.w3.org/2018/credentials#refreshService";
    public static final String PREDICATE_RELATED_RESOURCES = "https://www.w3.org/2018/credentials#relatedResource";
    public static final String PREDICATE_RENDER_METHOD = "https://www.w3.org/2018/credentials#renderMethod";
    public static final String PREDICATE_TERMS_OF_USE = "https://www.w3.org/2018/credentials#termsOfUse";

    public static final String PREDICATE_PROOF = "https://w3id.org/security#proof";

    SequencedCollection<?> context;

    URI id;

    Collection<String> type;

    // returns lang-map
    Map<String, String> name;

    // returns lang-map
    Map<String, String> description;

    Object issuer;

    Collection<?> subject;

    Instant validFrom;

    Instant validUntil;

    Collection<?> status;
    Collection<?> schema;
    Collection<?> evidence;

//        default Collection<Object> status() {
//            return List.of();
//        }
//
//        /**
//         * Checks if the credential is expired.
//         *
//         * @return <code>true</code> if the credential is expired
//         */
//        default boolean isExpired() {
//            return validUntil() != null && Instant.now().isAfter(validUntil());
//        }
//
//        /**
//         * Checks if the credential is active, i.e. does not define validFrom property
//         * or the property datetime is before now.
//         * 
//         * @since 0.90.0
//         * 
//         * @return <code>true</code> if the credential is active
//         */
//        default boolean isNotValidYet() {
//            return validFrom() != null && validFrom().isAfter(Instant.now());
//        }

    public interface Issuer {

    }

    public interface Evidence {

    }

    public interface Schema {

    }

    public interface Status {

    }

    public interface ConfidenceMethod {

    }

    public interface RefreshService {

    }

    public interface RenderMethod {

    }

    public interface TermsOfUse {

    }

//
//    public static GraphMapper newGraphMapper(TypeMapping typeMapping) {
//        return new GraphMapper(typeMapping);
//    }
//    
    public static class GraphMapper implements Graph.NodeMapper<Credential> {

        private final TypeMapping typeMapping;

        public GraphMapper() {
            this(null);
        }

        public GraphMapper(TypeMapping typeMapping) {
            this.typeMapping = typeMapping;
        }

        @Override
        public Credential materialize(
                Graph.Node root,
                Graph graph,
                SemanticModel model) {

            var credential = new Credential();

            if (!root.id().startsWith("_:")) {
                credential.id = URI.create(root.id());
            }

            for (var statement : root.statements()) {

                switch (statement.predicate()) {
                case Graph.PREDICATE_TYPE:
                    break;

                case PREDICATE_NAME:
                    credential.name = Graph.langMap(statement, credential.name, false);
                    break;

                case PREDICATE_DESCRIPTION:
                    credential.description = Graph.langMap(statement, credential.description, false);
                    break;

                case PREDICATE_ISSUER:
                    if (credential.issuer != null) {
                        throw new IllegalArgumentException();
                    }
                    credential.issuer = Graph.resource(statement, graph, model, Issuer.class, typeMapping);
                    break;

                case PREDICATE_VALID_FROM:
                    if (credential.validFrom != null) {
                        throw new IllegalArgumentException();
                    }
                    credential.validFrom = Graph.xsdDateTime(statement);
                    break;

                case PREDICATE_VALID_UNTIL:
                    if (credential.validUntil != null) {
                        throw new IllegalArgumentException();
                    }
                    credential.validUntil = Graph.xsdDateTime(statement);
                    break;

                case PREDICATE_SUBJECT:
                    if (credential.subject != null) {
                        throw new IllegalArgumentException();
                    }
                    credential.subject = Graph.resources(
                            statement,
                            credential.subject,
                            graph,
                            model,
                            Issuer.class, // FIXME
                            typeMapping);
                    break;

                case PREDICATE_STATUS:
                    if (credential.status != null) {
                        throw new IllegalArgumentException();
                    }
                    credential.status = Graph.resources(
                            statement,
                            credential.status,
                            graph,
                            model,
                            Status.class,
                            typeMapping);
                    break;
                
                case PREDICATE_PROOF:
                    IO.println("TODO: " + statement);
                    break;
                    
                default:
                    throw new IllegalArgumentException(
                            """
                            Unrecognized predicate has been found %s.
                            """.formatted(statement.predicate()));
                }
            }

            // TODO Auto-generated method stub
            return credential;
        }

    }
}
