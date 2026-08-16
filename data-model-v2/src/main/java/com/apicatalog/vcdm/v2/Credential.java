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

    public class GraphMapper implements Graph.NodeMapper<Credential> {

        private final TypeMapping typeMapping;

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

                case "https://schema.org/name":
                    credential.name = Graph.langMap(statement, credential.name);
                    break;

                case "https://schema.org/description":
                    credential.description = Graph.langMap(statement, credential.description);
                    break;

                case "https://www.w3.org/2018/credentials#issuer":
                    if (credential.issuer != null) {
                        throw new IllegalArgumentException();
                    }
                    credential.issuer = Graph.resource(statement, graph, model, Issuer.class, typeMapping);
                    break;

                case "https://www.w3.org/2018/credentials#validFrom":
                    if (credential.validFrom != null) {
                        throw new IllegalArgumentException();
                    }
                    credential.validFrom = Graph.xsdDateTime(statement);
                    break;

                case "https://www.w3.org/2018/credentials#validUntil":
                    if (credential.validUntil != null) {
                        throw new IllegalArgumentException();
                    }
                    credential.validUntil = Graph.xsdDateTime(statement);
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
