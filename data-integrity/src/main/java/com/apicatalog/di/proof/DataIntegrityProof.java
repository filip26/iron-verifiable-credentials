package com.apicatalog.di.proof;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.apicatalog.di.proof.c14n.StaticJCSCanonizer;
import com.apicatalog.di.proof.c14n.StaticRDFCCanonizer;
import com.apicatalog.di.suite.CryptoSuite;
import com.apicatalog.tree.io.Tree;
import com.apicatalog.tree.io.TreeEmitter;
import com.apicatalog.tree.io.java.NativeComposer;
import com.apicatalog.trust.lexical.PropertyProofMapper;
import com.apicatalog.trust.model.Model;
import com.apicatalog.trust.payload.PayloadGenerator;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.semantic.Graph;
import com.apicatalog.trust.semantic.Graph.LiteralStatement;
import com.apicatalog.trust.semantic.Graph.ResourceStatement;
import com.apicatalog.trust.semantic.GraphProofMapper;
import com.apicatalog.trust.semantic.SemanticModel;
import com.apicatalog.trust.semantic.SemanticModel.GraphCanonizer;
import com.apicatalog.trust.signature.Signature;

/**
 * Represents a W3C Data Integrity proof implementation.
 * 
 * This class encapsulates the properties and parameters of a cryptographic
 * proof as defined by the W3C Data Integrity specification.
 *
 * @see Proof
 */
public final class DataIntegrityProof implements Proof {

    public static final String TYPE_URI = "https://w3id.org/security#DataIntegrityProof";
    public static final String TYPE_NAME = "DataIntegrityProof";

    public static final String KEY_ID = "id";
    public static final String KEY_TYPE = "type";
    public static final String KEY_CRYPTOSUITE = "cryptosuite";
    public static final String KEY_CREATED = "created";
    public static final String KEY_EXPIRES = "expires";
    public static final String KEY_DOMAIN = "domain";
    public static final String KEY_CHALLENGE = "challenge";
    public static final String KEY_NONCE = "nonce";
    public static final String KEY_VERIFICATION_METHOD = "verificationMethod";
    public static final String KEY_PURPOSE = "proofPurpose";
    public static final String KEY_PROOF_VALUE = "proofValue";
    public static final String KEY_PREVIOUS_PROOF = "previousProof";

    public static final String PREDICATE_CRYPTOSUITE = "https://w3id.org/security#cryptosuite";
    public static final String PREDICATE_CREATED = "http://purl.org/dc/terms/created";
    public static final String PREDICATE_EXPIRES = "https://w3id.org/security#expiration";
    public static final String PREDICATE_DOMAIN = "https://w3id.org/security#domain";
    public static final String PREDICATE_CHALLENGE = "https://w3id.org/security#challenge";
    public static final String PREDICATE_NONCE = "https://w3id.org/security#nonce";
    public static final String PREDICATE_VERIFICATION_METHOD = "https://w3id.org/security#verificationMethod";
    public static final String PREDICATE_PROOF_PURPOSE = "https://w3id.org/security#proofPurpose";
    public static final String PREDICATE_PROOF_VALUE = "https://w3id.org/security#proofValue";
    public static final String PREDICATE_PREVIOUS_PROOF = "https://w3id.org/security#previousProof";

    private CryptoSuite cryptosuite;

    private SequencedCollection<String> context;

    private String id;
    private Instant created;
    private Instant expires;
    private Collection<String> domain;
    private String challenge;
    private String nonce;
    private String purpose;
    private String verificationMethod;
    private Signature proofValue;
    private Collection<String> previousProof;

    private byte[] canonicalPayload;

    private DataIntegrityProof() {
    }

    public static Map<String, ?> compact(DataIntegrityProof proof, boolean addContext) {
        var composer = new NativeComposer<Map<String, ? extends Object>>();
        compact(proof, composer, addContext);
        return composer.compose();
    }

    public static void compact(DataIntegrityProof proof, TreeEmitter emitter, boolean addContext) {

        var writer = Tree.newPropertyTree(emitter).beginMap();

        if (addContext && proof.context != null && !proof.context.isEmpty()) {

            writer.beginSequence("@context");

            for (var ctx : proof.context) {
                writer.element(ctx);
            }

            writer.endSequence();
        }

        writer.entry(KEY_ID, proof.id())
                .entry(KEY_TYPE, proof.type())
                .entry(KEY_CRYPTOSUITE, proof.cryptosuite(), CryptoSuite::id)
                .entry(KEY_CREATED, proof.created(), Instant::toString)
                .entry(KEY_EXPIRES, proof.expires(), Instant::toString);

        if (proof.domains() != null && !proof.domains().isEmpty()) {
            if (proof.domains().size() == 1) {
                writer.entry(KEY_DOMAIN, proof.domains().iterator().next());
            } else {
                writer.beginSequence(KEY_DOMAIN);
                for (var domain : proof.domains()) {
                    writer.element(domain);
                }
                writer.endSequence();
            }
        }

        writer.entry(KEY_CHALLENGE, proof.challenge())
                .entry(KEY_NONCE, proof.nonce())
                .entry(KEY_VERIFICATION_METHOD, proof.verificationMethod())
                .entry(KEY_PURPOSE, proof.purpose());

        if (proof.cryptosuite() != null) {
            writer.entry(KEY_PROOF_VALUE, proof.signature(), proof.cryptosuite()::encode);
        } else {
            writer.entry(KEY_PROOF_VALUE, proof.signature(), Signature::toString);
        }

        if (proof.previous() != null && !proof.previous().isEmpty()) {
            if (proof.previous().size() == 1) {
                writer.entry(KEY_PREVIOUS_PROOF, proof.previous().iterator().next());
            } else {
                writer.beginSequence(KEY_PREVIOUS_PROOF);
                for (var previousProof : proof.previous()) {
                    writer.element(previousProof);
                }
                writer.endSequence();
            }
        }
        writer.endMap();
    }

    /**
     * The unique identifier of the proof. Optional.
     * 
     * @return a string representing the URI of the proof ID, or {@code null} if not
     *         present
     */
    public String id() {
        return id;
    }

    /**
     * Retrieves the {@link CryptoSuite} used to create and verify the proof value.
     * 
     * @return the {@link CryptoSuite} attached to the proof, or {@code null} if not
     *         defined
     */
    public CryptoSuite cryptosuite() {
        return cryptosuite;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Instant created() {
        return created;
    }

    /**
     * The exact date and time when the proof expires. Optional.
     * 
     * @return an {@link Instant} representing the expiration timestamp, or
     *         {@code null} if it does not expire
     */
    public Instant expires() {
        return expires;
    }

    /**
     * A collection of strings specifying the restricted domain or domains of the
     * proof. Optional.
     *
     * @return a collection of strings representing the domains, or {@code null} if
     *         not present
     */
    public Collection<String> domains() {
        return domain;
    }

    /**
     * A string value used once for a particular domain and/or time. Used to
     * mitigate replay attacks. Optional.
     * 
     * @return the challenge string, or {@code null} if not present
     */
    public String challenge() {
        return challenge;
    }

    /**
     * A random or pseudo-random string value used to mitigate replay attacks.
     * Optional.
     * 
     * @return the nonce string, or {@code null} if not present
     */
    public String nonce() {
        return nonce;
    }

    /**
     * A collection of strings identifying previous proofs. Used to create a chain
     * of proofs. Optional.
     * 
     * @return a collection of strings representing the URIs of previous proofs, or
     *         {@code null} if not present
     */
    public Collection<String> previous() {
        return previousProof;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] canonicalPayload() {
        return canonicalPayload;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String c14n() {
        return cryptosuite != null ? cryptosuite.c14n() : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String type() {
        return TYPE_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signature signature() {
        return proofValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String verificationMethod() {
        return verificationMethod;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String purpose() {
        return purpose;
    }

    /**
     * The JSON-LD context used to process the proof. Optional.
     * 
     * @return a collection of strings representing the JSON-LD context URIs, or
     *         {@code null} if not present
     */
    public SequencedCollection<String> context() {
        return context;
    }

    public boolean isValid() {
        return created != null
                && Instant.now().isAfter(created)
                && (expires == null || Instant.now().isBefore(expires))
                && verificationMethod != null
                && purpose != null
                && proofValue != null
        // TODO && proofValue.isValid()
        ;
    }

//    public void validate(Map<String, Object> params) {
//
//        Objects.requireNonNull(created, "");
//        Objects.requireNonNull(verificationMethod, "");
//        Objects.requireNonNull(purpose, "");
//        Objects.requireNonNull(proofValue, "");
//
//        if (params != null) {
    //// assertEquals(params, DataIntegrityVocab.PURPOSE, purpose.toString()); //
    /// TODO compare as URI, expect URI in params / assertEquals(params,
    /// DataIntegrityVocab.CHALLENGE, challenge); / assertEquals(params,
    /// DataIntegrityVocab.DOMAIN, domain);
//        }
//    }

//    protected static void assertEquals(Map<String, Object> params, Term name, String param) throws DocumentError {
//        final Object value = params.get(name.name());
//
//        if (value == null) {
//            return;
//        }
//
//        if (!value.equals(param)) {
//            throw new DocumentError(ErrorType.Invalid, name);
//        }
//    }

    public static Function<DataIntegrityProof, byte[]> getProofCanonizer(String c14n) {
        return switch (c14n) {
        case Model.C14N_JCS -> StaticJCSCanonizer::canonize;
        case Model.C14N_RDFC -> StaticRDFCCanonizer::canonize;
        default -> throw new IllegalArgumentException();
        };
    }

    public static class Draft {

        protected final DataIntegrityProof proof;

        // TODO public?!
        public Draft(CryptoSuite cryptosuite) {
            this.proof = new DataIntegrityProof();
            this.proof.cryptosuite = cryptosuite;
        }

        protected byte[] canonize(String c14n) {
            return canonize(DataIntegrityProof.getProofCanonizer(c14n));
        }

        protected byte[] canonize(Function<DataIntegrityProof, byte[]> canonizer) {

            Objects.requireNonNull(canonizer);

            proof.canonicalPayload = canonizer.apply(proof);
            return proof.canonicalPayload;
        }

        // TODO clone?
        public Draft proof(DataIntegrityProof source) {
            proof.canonicalPayload = source.canonicalPayload;
            proof.challenge = source.challenge;
            proof.context = source.context;
            proof.created = source.created;
            proof.cryptosuite = source.cryptosuite;
            proof.domain = source.domain;
            proof.expires = source.expires;
            proof.id = source.id;
            proof.nonce = source.nonce;
            proof.previousProof = source.previousProof;
            proof.purpose = source.purpose;
            proof.verificationMethod = source.verificationMethod;
            return this;
        }

        public boolean isValid() {
            return proof.isValid();
        }

        // TODO ?!?!
        public Draft options(Map<String, Object> options) {

            previousProof(Set.of());

            for (var entry : options.entrySet()) {
                switch (entry.getKey()) {
                case "@context":
                    if (entry.getValue() instanceof Collection<?> col) {
                        context(col.stream().map(String.class::cast).toList());

                    } else if (entry.getValue() instanceof String uri) {
                        context(List.of(uri));

                    } else {
                        throw new IllegalArgumentException();
                    }
                    break;
                case KEY_ID:
                    id((String) entry.getValue());
                    break;
                case KEY_CREATED:
                    created(Instant.parse((String) entry.getValue()));
                    break;
                case KEY_EXPIRES:
                    expires(Instant.parse((String) entry.getValue()));
                    break;
                case KEY_PURPOSE:
                    purpose((String) entry.getValue());
                    break;
                case KEY_VERIFICATION_METHOD:
                    verificationMethod((String) entry.getValue());
                    break;
                case KEY_NONCE:
                    nonce((String) entry.getValue());
                    break;
                case KEY_CHALLENGE:
                    challenge((String) entry.getValue());
                    break;
                case KEY_DOMAIN:
                    if (entry.getValue() instanceof Collection<?> col) {
                        domain(col.stream().map(String.class::cast).toList());

                    } else if (entry.getValue() instanceof String uri) {
                        domain(List.of(uri));

                    } else {
                        throw new IllegalArgumentException();
                    }
                    break;
                case KEY_PREVIOUS_PROOF:
                    if (entry.getValue() instanceof Collection<?> col) {
                        previousProof(col.stream().map(String.class::cast).toList());

                    } else if (entry.getValue() instanceof String uri) {
                        previousProof(List.of(uri));

                    } else {
                        throw new IllegalArgumentException();
                    }

                    break;
                }
            }
            return this;
        }

        public Draft created(Instant created) {
            proof.created = created != null
                    ? created.truncatedTo(ChronoUnit.SECONDS)
                    : null;
            return this;
        }

        public Draft expires(Instant expires) {
            proof.expires = expires != null
                    ? expires.truncatedTo(ChronoUnit.SECONDS)
                    : null;
            return this;
        }

        public Draft purpose(String purpose) {
            proof.purpose = purpose;
            return this;
        }

        public Draft verificationMethod(String verificationMethod) {
            proof.verificationMethod = verificationMethod;
            return this;
        }

        public Draft id(String id) {
            proof.id = id;
            return this;
        }

        public Draft challenge(String challenge) {
            proof.challenge = challenge;
            return this;
        }

        public Draft nonce(String nonce) {
            proof.nonce = nonce;
            return this;
        }

        public Draft domain(Collection<String> domain) {
            proof.domain = domain;
            return this;
        }

        public Draft previousProof(Collection<String> previousProof) {
            proof.previousProof = previousProof;
            return this;
        }

        public DataIntegrityProof unsigned() {
            return proof;
        }

        public DataIntegrityProof signed(Signature signature) {
            proof.proofValue = signature;
            return proof;
        }

        public Draft context(SequencedCollection<String> context) {
            proof.context = context;
            return this;
        }

        public CryptoSuite cryptosuite() {
            return proof.cryptosuite;
        }

        public Collection<String> context() {
            return proof.context();
        }

        public Collection<String> previous() {
            return proof.previous() != null ? proof.previous() : Set.of();
        }
    }

    public static class PropertyMapper implements PropertyProofMapper {

        private final Map<String, CryptoSuite> cryptosuites;

        public PropertyMapper(Map<String, CryptoSuite> cryptosuites) {
            this.cryptosuites = cryptosuites;
        }

        @Override
        public boolean accepts(Map<String, Object> proof) {
            return TYPE_NAME.equals(proof.get(KEY_TYPE))
                    && cryptosuites.containsKey(proof.get(KEY_CRYPTOSUITE));
        }

        @Override
        public Proof materialize(
                Collection<String> contexts,
                Map<String, Object> proof,
                byte[] proofPayload,
                PayloadGenerator payload) {

            final var di = new DataIntegrityProof();
            di.canonicalPayload = proofPayload;

            String proofValue = null;

            for (var entry : proof.entrySet()) {
                switch (entry.getKey()) {
                case KEY_ID:
                    di.id = stringValue(entry.getValue());
                    break;
                case KEY_TYPE, "@context":
                    // skip, already processed
                    break;
                case KEY_CRYPTOSUITE:
                    di.cryptosuite = cryptosuites.get(entry.getValue());
                    break;
                case KEY_CREATED:
                    di.created = value(entry.getValue(), Instant::parse);
                    break;
                case KEY_EXPIRES:
                    di.expires = value(entry.getValue(), Instant::parse);
                    break;
                case KEY_DOMAIN:
                    if (entry.getValue() instanceof String value) {
                        di.domain = List.of(value);

                    } else if (proof.get(KEY_DOMAIN) instanceof Collection<?> col) {
                        di.domain = col.stream().map(String.class::cast).toList();

                    } else {
                        throw new IllegalArgumentException();
                    }
                    break;
                case KEY_CHALLENGE:
                    di.challenge = stringValue(entry.getValue());
                    break;
                case KEY_NONCE:
                    di.nonce = stringValue(entry.getValue());
                    break;
                case KEY_PURPOSE:
                    di.purpose = stringValue(entry.getValue());
                    break;
                case KEY_VERIFICATION_METHOD:
                    di.verificationMethod = stringValue(entry.getValue());
                    break;
                case KEY_PROOF_VALUE:
                    proofValue = stringValue(entry.getValue());
                    break;
                case KEY_PREVIOUS_PROOF:
                    if (entry.getValue() instanceof String value) {
                        di.previousProof = List.of(value);

                    } else if (proof.get(KEY_PREVIOUS_PROOF) instanceof Collection<?> col) {
                        di.previousProof = col.stream().map(String.class::cast).toList();

                    } else {
                        throw new IllegalArgumentException();
                    }
                    break;
                default:
                    throw new IllegalArgumentException(
                            """
                            Unsupported DI proof property %s.
                            """.formatted(entry.getKey()));
                }
            }
            if (di.previousProof == null) {
                di.previousProof = Set.of();

            } else if (!di.previousProof.isEmpty()) {
                payload.withProofs(di.previousProof);
            }

            if (proofValue != null) {
                di.proofValue = di.cryptosuite
                        .decode(
                                proofValue,
                                di,
                                payload);
            }

            return di;
        }

        private static String stringValue(Object object) {
            return value(object, Function.identity());
        }

        private static <T> T value(Object object, Function<String, T> fnc) {
            if (object instanceof String value) {
                return fnc.apply(value);

            }
            throw new IllegalArgumentException();
        }

    }

    public static class GraphMapper implements GraphProofMapper {

        private final Map<String, CryptoSuite> cryptosuites;
        private final Supplier<GraphCanonizer> canonizeFactory;

        public GraphMapper(Map<String, CryptoSuite> cryptosuites, Supplier<GraphCanonizer> canonizeFactory) {
            this.cryptosuites = cryptosuites;
            this.canonizeFactory = canonizeFactory;
        }

        @Override
        public boolean accepts(Graph.Node proofNode) {

            if (proofNode.type().size() != 1
                    || !TYPE_URI.equals(proofNode.type().getFirst())) {
                return false;
            }

            for (var statement : proofNode.statements()) {
                if (PREDICATE_CRYPTOSUITE.equals(statement.predicate())
                        && statement instanceof LiteralStatement literal
                        && cryptosuites.containsKey(literal.object())) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public Proof materialize(
                String id,
                Graph proofGraph,
                SemanticModel model,
                PayloadGenerator payload) {

            var proofNode = proofGraph.nodes().get(id);

            if (proofNode.type().size() != 1) {
                throw new IllegalArgumentException();
            }

            if (proofGraph.nodes().size() != 1) {
                throw new IllegalArgumentException(
                        "Only one node is allowed per proof graph; found " + proofGraph.nodes().size() + " nodes");
            }

            final var proofType = proofNode.type().getFirst();

            if (!TYPE_URI.equals(proofType)) {
                throw new IllegalArgumentException(
                        """
                        An unexpected proof type has been detected %s, expected %s.
                        """.formatted(proofType, TYPE_URI));
            }

            final var di = new DataIntegrityProof();

            if (!proofNode.id().startsWith("_:")) {
                di.id = proofNode.id();
            }
            di.previousProof = Set.of();

            final var canonizer = canonizeFactory.get();

            String proofValue = null;

            for (var statement : proofNode.statements()) {

                boolean canonizeStatement = true;

                switch (statement.predicate()) {
                case Graph.PREDICATE_TYPE:
                    break;

                case PREDICATE_CRYPTOSUITE:
                    if (!(statement instanceof LiteralStatement literal)) {
                        throw new IllegalArgumentException();
                    }
                    if (!"https://w3id.org/security#cryptosuiteString".equals(literal.datatype())) {
                        throw new IllegalArgumentException();
                    }

                    di.cryptosuite = cryptosuites.get(literal.object());
                    break;

                case PREDICATE_CREATED:
                    if (!(statement instanceof LiteralStatement literal)) {
                        throw new IllegalArgumentException();
                    }
                    if (!"http://www.w3.org/2001/XMLSchema#dateTime".equals(literal.datatype())) {
                        throw new IllegalArgumentException();
                    }

                    di.created = Instant.parse(literal.object());
                    break;

                case PREDICATE_EXPIRES:
                    if (!(statement instanceof LiteralStatement literal)) {
                        throw new IllegalArgumentException();
                    }
                    if (!"http://www.w3.org/2001/XMLSchema#dateTime".equals(literal.datatype())) {
                        throw new IllegalArgumentException();
                    }

                    di.expires = Instant.parse(literal.object());
                    break;

                case PREDICATE_NONCE:
                    if (!(statement instanceof LiteralStatement literal)) {
                        throw new IllegalArgumentException();
                    }
                    // TODO check datatype
                    di.nonce = literal.object();
                    break;

                case PREDICATE_CHALLENGE:
                    if (!(statement instanceof LiteralStatement literal)) {
                        throw new IllegalArgumentException();
                    }
                    // TODO check datatype
                    di.challenge = literal.object();
                    break;

                case PREDICATE_DOMAIN:
                    if (!(statement instanceof LiteralStatement literal)) {
                        throw new IllegalArgumentException();
                    }
                    // TODO check datatype

                    if (di.domain == null) {
                        di.domain = new ArrayList<>();
                    }

                    di.domain.add(literal.object());
                    break;

                case PREDICATE_PROOF_PURPOSE:
                    // TODO checks

                    di.purpose = statement.object().substring("https://w3id.org/security#".length());
                    break;

                case PREDICATE_VERIFICATION_METHOD:
                    if (!(statement instanceof ResourceStatement resource)) {
                        throw new IllegalArgumentException();
                    }

                    di.verificationMethod = resource.object();
                    break;

                case PREDICATE_PROOF_VALUE:
                    if (!(statement instanceof LiteralStatement literal)) {
                        throw new IllegalArgumentException();
                    }

                    // TODO "https://w3id.org/security#multibase"
                    canonizeStatement = false;
                    proofValue = literal.object();
                    break;

                case PREDICATE_PREVIOUS_PROOF:
                    if (!(statement instanceof ResourceStatement resource)) {
                        throw new IllegalArgumentException();
                    }

                    if (di.previousProof.isEmpty()) {
                        di.previousProof = new ArrayList<String>();
                    }

                    di.previousProof.add(resource.object());
                    break;

                default:
                    throw new IllegalArgumentException(
                            """
                            Unrecognized proof predicate has been found %s.
                            """.formatted(statement.predicate()));
                }

                if (canonizeStatement) {
                    canonizer.accept(
                            proofNode.id(),
                            statement.predicate(),
                            statement.object(),
                            statement.datatype(),
                            statement.language(),
                            statement.direction(),
                            null);
                }
            }

            if (!di.previousProof.isEmpty()) {
                payload.withProofs(di.previousProof);
            }

            di.canonicalPayload = canonizer.canonize();

            if (proofValue != null) {
                di.proofValue = di.cryptosuite
                        .decode(
                                proofValue,
                                di,
                                payload);
            }

            return di;
        }
    }
}
