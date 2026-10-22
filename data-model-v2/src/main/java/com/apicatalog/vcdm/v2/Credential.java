package com.apicatalog.vcdm.v2;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.Set;

import com.apicatalog.trust.LangString;
import com.apicatalog.trust.semantic.Graph;
import com.apicatalog.trust.semantic.Graph.NodeMapping;
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

    Set<String> type;

    SequencedCollection<LangString> name;

    SequencedCollection<LangString> description;

    Object issuer;

    Collection<?> subject;

    Instant validFrom;

    Instant validUntil;

    Collection<?> status;
    Collection<?> schema;
    Collection<?> evidence;
    Collection<?> termsOfUse;

    /**
     * Checks whether all mandatory properties of the credential are present,
     * excluding the proofs itself.
     *
     * @return {@code true} if all required properties are present, {@code false}
     *         otherwise
     */
    public boolean hasRequired() {
        return context != null && VCDM2.isDefined(context)
                && type != null && type.contains(TYPE_URI)
                && issuer != null
                && subject != null && !subject.isEmpty()
        // TODO
        ;
    }

    /**
     * Checks whether the credential has expired according to its temporal
     * properties and the current system time.
     *
     * @return {@code true} if the credential is expired, {@code false} otherwise
     */
    public boolean isExpired() {
        return validUntil != null && validUntil.isBefore(Instant.now());
    }

    /**
     * Checks whether the credential is post-dated ({@link Credential#validFrom()}
     * in the future relative to the current system time).
     *
     * @return {@code true} if the credential's validFrom time is in the future,
     *         {@code false} otherwise
     */
    public boolean isPostDated() {
        return validFrom != null && Instant.now().isBefore(validFrom);
    }

    /**
     * The JSON-LD context used to process the credentials.
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

    public Collection<LangString> name() {
        return name;
    }

    public Collection<LangString> description() {
        return description;
    }

    public Instant validFrom() {
        return validFrom;
    }

    public Instant validUntil() {
        return validUntil;
    }

    public Object issuer() {
        return issuer;
    }

    public Collection<?> subject() {
        return subject;
    }

    public Collection<?> status() {
        return status;
    }

    public static class GraphMapper implements Graph.NodeMapper<Credential> {

        private final NodeMapping typeMapping;

        public GraphMapper() {
            this(null);
        }

        public GraphMapper(NodeMapping typeMapping) {
            this.typeMapping = typeMapping;
        }

        @Override
        public Credential materialize(
                SequencedCollection<?> context,
                Graph.Node node,
                Map<String, Graph> dataset,
                SemanticModel model) {

            var credential = new Credential();
            credential.context = context;

            if (!node.id().startsWith("_:")) {
                credential.id = URI.create(node.id());
            }

            for (var statement : node.statements()) {

                switch (statement.predicate()) {
                case Graph.PREDICATE_TYPE:
                    credential.type = Graph.resources(statement, credential.type);
                    break;

                case PREDICATE_NAME:
                    credential.name = Graph.langString(statement, credential.name, false);
                    break;

                case PREDICATE_DESCRIPTION:
                    credential.description = Graph.langString(statement, credential.description, false);
                    break;

                case PREDICATE_ISSUER:
                    if (credential.issuer != null) {
                        throw new IllegalArgumentException();
                    }
                    credential.issuer = Graph.node(context, statement, node.graph(), dataset, model, typeMapping);
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
                    credential.subject = Graph.nodes(
                            context,
                            statement,
                            credential.subject,
                            node.graph(),
                            dataset,
                            model,
                            typeMapping);
                    break;

                case PREDICATE_STATUS:
                    credential.status = Graph.nodes(
                            context,
                            statement,
                            credential.status,
                            node.graph(),
                            dataset,
                            model,
                            typeMapping);
                    break;

                case PREDICATE_SCHEMA:
                    credential.schema = Graph.nodes(
                            context,
                            statement,
                            credential.schema,
                            node.graph(),
                            dataset,
                            model,
                            typeMapping);
                    break;

                case PREDICATE_TERMS_OF_USE:
                    credential.termsOfUse = Graph.nodes(
                            context,
                            statement,
                            credential.termsOfUse,
                            node.graph(),
                            dataset,
                            model,
                            typeMapping);
                    break;

                case PREDICATE_EVIDENCE:
                    credential.evidence = Graph.nodes(
                            context,
                            statement,
                            credential.evidence,
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

            return credential;
        }
    }

//  public interface Issuer {
//
//  }
//
//  public interface Evidence {
//
//  }
//
//  public interface Schema {
//
//  }
//
//  public interface Status {
//
//  }
//
//  public interface ConfidenceMethod {
//
//  }
//
//  public interface RefreshService {
//
//  }
//
//  public interface RenderMethod {
//
//  }
//
//  public interface TermsOfUse {
//
//  }

}
