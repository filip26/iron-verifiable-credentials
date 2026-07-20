package com.apicatalog.di.proof;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import com.apicatalog.di.suite.CryptoSuite;
import com.apicatalog.tree.io.Tree;
import com.apicatalog.tree.io.TreeEmitter;
import com.apicatalog.trust.lexical.MapProofReader;
import com.apicatalog.trust.payload.PayloadGenerator;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.semantic.GraphProofReader;
import com.apicatalog.trust.semantic.SemanticModel;
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

    private static final String KEY_ID = "id";
    private static final String KEY_TYPE = "type";
    private static final String KEY_CRYPTOSUITE = "cryptosuite";
    private static final String KEY_CREATED = "created";
    private static final String KEY_EXPIRES = "expires";
    private static final String KEY_DOMAIN = "domain";
    private static final String KEY_CHALLENGE = "challenge";
    private static final String KEY_NONCE = "nonce";
    private static final String KEY_VERIFICATION_METHOD = "verificationMethod";
    private static final String KEY_PURPOSE = "proofPurpose";
    private static final String KEY_PROOF_VALUE = "proofValue";
    private static final String KEY_PREVIOUS_PROOF = "previousProof";

    private static final String PREDICATE_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
    private static final String PREDICATE_CRYPTOSUITE = "https://w3id.org/security#cryptosuite";
    private static final String PREDICATE_CREATED = "http://purl.org/dc/terms/created";
    private static final String PREDICATE_EXPIRES = "";
    private static final String PREDICATE_DOMAIN = "";
    private static final String PREDICATE_CHALLENGE = "";
    private static final String PREDICATE_NONCE = "";
    private static final String PREDICATE_VERIFICATION_METHOD = "https://w3id.org/security#verificationMethod";
    private static final String PREDICATE_PROOF_PURPOSE = "https://w3id.org/security#proofPurpose";
    private static final String PREDICATE_PROOF_VALUE = "https://w3id.org/security#proofValue";
    private static final String PREDICATE_PREVIOUS_PROOF = "https://w3id.org/security#previousProof";

    private CryptoSuite cryptosuite;

    private Collection<String> context;

    private String id;
    private Instant created;
    private Instant expires;
    private Collection<String> domain;
    private String challenge;
    private String nonce;
    private String purpose;
    private String verificationMethod;
    private Signature signature;
    private Collection<String> previousProof;

    private byte[] canonicalPayload;

    private DataIntegrityProof() {
    }

    public static void write(DataIntegrityProof proof, TreeEmitter emitter) {

        var writer = Tree.newPropertyTree(emitter)
                .beginMap()
                .entry(KEY_ID, proof.id())
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
        return signature;
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
    public Collection<String> context() {
        return context;
    }

    public static class Draft {

        protected final DataIntegrityProof proof;

        // TODO public?!
        public Draft(CryptoSuite cryptosuite) {
            this.proof = new DataIntegrityProof();
            this.proof.cryptosuite = cryptosuite;
        }

        protected byte[] canonize(String c14n) {
            return canonize(DIProofC14NTemplates.getSignTemplate(c14n));
        }

        protected byte[] canonize(Function<DataIntegrityProof, byte[]> canonizer) {

            Objects.requireNonNull(canonizer);

            proof.canonicalPayload = canonizer.apply(proof);
            return proof.canonicalPayload;
        }

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

        public Draft previousProof(Collection<String> previousProof) {
            proof.previousProof = previousProof;
            return this;
        }

        public DataIntegrityProof unsigned() {
            return proof;
        }

        public DataIntegrityProof signed(Signature signature) {
            proof.signature = signature;
            return proof;
        }

        public Draft context(Collection<String> context) {
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

    public static class MapReader implements MapProofReader {

        private final Map<String, CryptoSuite> cryptosuites;

        public MapReader(Map<String, CryptoSuite> cryptosuites) {
            this.cryptosuites = cryptosuites;
        }

        @Override
        public boolean isAccepted(Map<String, Object> proof) {
            return TYPE_NAME.equals(proof.get(KEY_TYPE))
                    && cryptosuites.containsKey(proof.get(KEY_CRYPTOSUITE));
        }

        @Override
        public Proof read(
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
                di.signature = di.cryptosuite
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

    public static class GraphReader implements GraphProofReader {

        private final Map<String, CryptoSuite> cryptosuites;

        public GraphReader(Map<String, CryptoSuite> cryptosuites) {
            this.cryptosuites = cryptosuites;
        }

        @Override
        public boolean isAccepted(Collection<String[]> proof) {

            for (var statement : proof) {
                if (PREDICATE_CRYPTOSUITE.equals(statement[1]) && cryptosuites.containsKey(statement[2])) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Proof read(
                Collection<String[]> proof,
                SemanticModel model,
                PayloadGenerator payload) {
            final var di = new DataIntegrityProof();

            var canonizer = model.newCanonizer();

            var consumer = canonizer.consumer();

            String proofValue = null;

            for (var statement : proof) {
                switch (statement[1]) {
                case PREDICATE_TYPE:
                    if (!TYPE_URI.equals(statement[2])) {
                        throw new IllegalArgumentException(
                                """
                                An unexpected proof type has been detected %s, expected %s.
                                """.formatted(statement[1], TYPE_URI));
                    }
                    if (!statement[0].startsWith("_:")) {
                        di.id = statement[0];
                    }
                    break;
                case PREDICATE_CRYPTOSUITE:
                    di.cryptosuite = cryptosuites.get(statement[2]);
                    break;
                case PREDICATE_CREATED:
                    di.created = Instant.parse(statement[2]);
                    break;
                case PREDICATE_PROOF_PURPOSE:
                    di.purpose = statement[2].substring("https://w3id.org/security#".length());
                    break;
                case PREDICATE_VERIFICATION_METHOD:
                    di.verificationMethod = statement[2];
                    break;
                case PREDICATE_PROOF_VALUE:
                    proofValue = statement[2];
                    break;
                case PREDICATE_PREVIOUS_PROOF:
                    if (di.previousProof == null) {
                        di.previousProof = new ArrayList<String>();
                    }
                    di.previousProof.add(statement[2]);
                    break;
                default:
                    throw new IllegalArgumentException(
                            """
                            Unrecognized proof predicate has been found %s.
                            """.formatted(statement[1]));
                }
                if (!PREDICATE_PROOF_VALUE.equals(statement[1])) { // TODO better
                    consumer.accept(statement[0], statement[1], statement[2], statement[3], statement[4], statement[5],
                            null);
                }
            }

            if (di.previousProof == null) {
                di.previousProof = Set.of();

            } else if (!di.previousProof.isEmpty()) {
                payload.withProofs(di.previousProof);
            }

            di.canonicalPayload = canonizer.canonize();

            if (proofValue != null) {
                di.signature = di.cryptosuite
                        .decode(
                                proofValue,
                                di,
                                payload);
            }
            return di;
        }
    }
}
