package com.apicatalog.di.proof;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import com.apicatalog.di.suite.CryptoSuite;
import com.apicatalog.tree.io.Tree;
import com.apicatalog.tree.io.TreeEmitter;
import com.apicatalog.trust.model.DataModel;
import com.apicatalog.trust.model.SemanticModel;
import com.apicatalog.trust.processor.PayloadProcessor;
import com.apicatalog.trust.proof.GraphProofReader;
import com.apicatalog.trust.proof.MapProofReader;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.signature.Signature;

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
    }

    public static Builder newDraft(CryptoSuite cryptosuite) {
        var di = new DataIntegrityProof();
        di.cryptosuite = cryptosuite;
        return new Builder(di);
    }

    public static Builder newBuilder(Map<String, Object> options, Function<String, CryptoSuite> suiteProvider) {

        var cryptosuite = (String) options.get("cryptosuite");

        var di = new DataIntegrityProof();
        di.cryptosuite = suiteProvider.apply(cryptosuite);
        var draft = new Builder(di);
        draft.previousProof(Set.of());

        for (var entry : options.entrySet()) {
            switch (entry.getKey()) {
            case "@context":
                if (entry.getValue() instanceof Collection<?> col) {
                    draft.context(col.stream().map(String.class::cast).toList());

                } else if (entry.getValue() instanceof String uri) {
                    draft.context(List.of(uri));

                } else {
                    throw new IllegalArgumentException();
                }
                break;
            case KEY_ID:
                draft.id((String) entry.getValue());
                break;
            case KEY_CREATED:
                draft.created(Instant.parse((String) entry.getValue()));
                break;
            case KEY_EXPIRES:
                draft.expires(Instant.parse((String) entry.getValue()));
                break;
            case KEY_PURPOSE:
                draft.purpose((String) entry.getValue());
                break;
            case KEY_VERIFICATION_METHOD:
                draft.verificationMethod((String) entry.getValue());
                break;
            case KEY_PREVIOUS_PROOF:
                if (entry.getValue() instanceof Collection<?> col) {
                    draft.previousProof(col.stream().map(String.class::cast).toList());

                } else if (entry.getValue() instanceof String uri) {
                    draft.previousProof(List.of(uri));

                } else {
                    throw new IllegalArgumentException();
                }

                break;
            }
        }

        return draft;
    }

    public String id() {
        return id;
    }

    public CryptoSuite cryptosuite() {
        return cryptosuite;
    }

    public Instant created() {
        return created;
    }

    public Instant expires() {
        return expires;
    }

    public Collection<String> domains() {
        return domain;
    }

    public String challenge() {
        return challenge;
    }

    public String nonce() {
        return nonce;
    }

    @Override
    public Collection<String> previous() {
        return previousProof;
    }

    @Override
    public byte[] canonicalPayload() {
        return canonicalPayload;
    }

    @Override
    public String c14n() {
        return cryptosuite != null ? cryptosuite.c14n() : null;
    }

    @Override
    public String type() {
        return TYPE_NAME;
    }

    @Override
    public Signature signature() {
        return signature;
    }

    @Override
    public String verificationMethod() {
        return verificationMethod;
    }

    @Override
    public String purpose() {
        return purpose;
    }

    public Collection<String> context() {
        return context;
    }

    private static final byte[][] RDFC_TEMPLATE = Stream.of(
            "_:c14n0",
            " <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://w3id.org/security#DataIntegrityProof> .",

            " <http://purl.org/dc/terms/created> \"",
            "\"^^<http://www.w3.org/2001/XMLSchema#dateTime> .",

            " <https://vc.ex/1> <https://w3id.org/security#challenge> \"",
            "\" .",

            " <https://w3id.org/security#cryptosuite> \"",
            "\"^^<https://w3id.org/security#cryptosuiteString> .",

            " <https://w3id.org/security#domain> \"",
            "\" .",

            " <https://w3id.org/security#expiration> \"",
            "\"^^<http://www.w3.org/2001/XMLSchema#dateTime> .",

            " <https://w3id.org/security#nonce> \"",
            "\" .",

            " <https://w3id.org/security#previousProof> <",
            "> .",

            " <https://w3id.org/security#proofPurpose> <https://w3id.org/security#",
            "> .",

            " <https://w3id.org/security#verificationMethod> <",
            "> .")
            .map(i -> i.getBytes(StandardCharsets.UTF_8))
            .toArray(byte[][]::new);

    private static final byte[][] JCS_TEMPLATE = Stream.of(
            "\"type\":\"DataIntegrityProof\"",

            "\"challenge\":\"",
            "\"created\":\"",
            "\"cryptosuite\":\"",
            "\"domain\":",
            "\"expires\":\"",
            "\"id\":\"",
            "\"nonce\":\"",
            "\"previousProof\":\"",
            "\"proofPurpose\":\"",
            "\"verificationMethod\":\"",
            "\"@context\":")
            .map(i -> i.getBytes(StandardCharsets.UTF_8))
            .toArray(byte[][]::new);

    private static Function<DataIntegrityProof, byte[]> getSignTemplate(String c14n) {
        return switch (c14n) {
        case DataModel.C14N_JCS -> DataIntegrityProof::jcs;
        case DataModel.C14N_RDFC -> DataIntegrityProof::rdfc;
        default -> throw new IllegalArgumentException();
        };
    }

    private static byte[] jcs(DataIntegrityProof proof) {
        return jcs(proof, false, false);
    }

    /**
     * Builds the canonical JSON proof (JCS) for hashing/signing.
     *
     * @param proof
     * @param singleElementContext
     * @param singleElementDomain
     * @return UTF-8 encoded JSON proof bytes
     */
    private static byte[] jcs(DataIntegrityProof proof, boolean singleElementContext, boolean singleElementDomain) {
        try {
            var os = new ByteArrayOutputStream();
            os.write('{');

            var next = false;

            if (proof.context() != null && !proof.context().isEmpty()) {
                os.write(JCS_TEMPLATE[11]);
                if (!singleElementContext && proof.context().size() == 1) {
                    os.write('"');
                    os.write(escape(proof.context().iterator().next()));
                    os.write('"');
                } else {
                    os.write('[');
                    boolean first = true;
                    for (var context : proof.context()) {
                        if (!first) {
                            os.write(',');
                        } else {
                            first = false;
                        }
                        os.write('"');
                        os.write(escape(context));
                        os.write('"');
                    }
                    os.write(']');
                }
                next = true;
            }
            next = writeJcsEntry(1, proof.challenge(), os, next);
            next = writeJcsEntry(2, proof.created(), Instant::toString, os, next);
            next = writeJcsEntry(3, proof.cryptosuite(), CryptoSuite::id, os, next);
            if (proof.domains() != null && !proof.domains().isEmpty()) {
                if (next) {
                    os.write(',');
                }
                os.write(JCS_TEMPLATE[4]);
                if (!singleElementDomain && proof.domains().size() == 1) {
                    os.write('"');
                    os.write(escape(proof.domains().iterator().next()));
                    os.write('"');
                } else {
                    os.write('[');
                    boolean first = true;
                    for (var domain : proof.domains()) {
                        if (!first) {
                            os.write(',');
                        } else {
                            first = false;
                        }
                        os.write('"');
                        os.write(escape(domain));
                        os.write('"');
                    }
                    os.write(']');
                }
                next = true;
            }
            next = writeJcsEntry(5, proof.expires(), Instant::toString, os, next);
            next = writeJcsEntry(6, proof.id(), os, next);
            next = writeJcsEntry(7, proof.nonce(), os, next);

            if (proof.previous() != null && !proof.previous().isEmpty()) {
                if (next) {
                    os.write(',');
                }
                os.write(JCS_TEMPLATE[8]);
                if (!singleElementDomain && proof.previous().size() == 1) {
                    os.write('"');
                    os.write(escape(proof.previous().iterator().next()));
                    os.write('"');
                } else {
                    os.write('[');
                    boolean first = true;
                    for (var previous : proof.previous()) {
                        if (!first) {
                            os.write(',');
                        } else {
                            first = false;
                        }
                        os.write('"');
                        os.write(escape(previous));
                        os.write('"');
                    }
                    os.write(']');
                }
                next = true;
            }
            next = writeJcsEntry(9, proof.purpose(), os, next);

            if (next) {
                os.write(',');
            }
            os.write(JCS_TEMPLATE[0]); // type
            writeJcsEntry(10, proof.verificationMethod(), os, true);

            os.write('}');

            return os.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Builds the deterministic N-Quads representation of a DataIntegrityProof for
     * RDF Dataset Canonicalization (RDFC).
     *
     * <p>
     * The returned value is UTF-8 encoded and suitable for hashing or signing. The
     * output strictly follows N-Quads syntax and is deterministic for the supplied
     * values.
     * </p>
     *
     * @param proof
     * @return UTF-8 encoded canonical N-Quads proof representation
     */
    private static byte[] rdfc(DataIntegrityProof proof) {

        byte[] id = proof.id() != null
                ? ("<" + proof.id() + ">").getBytes(StandardCharsets.UTF_8)
                : RDFC_TEMPLATE[0];

        try {
            var os = new ByteArrayOutputStream();

            writeRdfcEntry(id, 2, proof.created(), Instant::toString, os);

            os.write(id);
            os.write(RDFC_TEMPLATE[1]);
            os.write('\n');

            writeRdfcEntry(id, 4, proof.challenge(), os);
            writeRdfcEntry(id, 6, proof.cryptosuite(), CryptoSuite::id, os);

            if (proof.domains() != null && !proof.domains().isEmpty()) {
                for (var domain : proof.domains()) {
                    writeRdfcEntry(id, 8, domain, os);
                }
            }

            writeRdfcEntry(id, 10, proof.expires(), Instant::toString, os);
            writeRdfcEntry(id, 12, proof.nonce(), os);

            if (proof.previous() != null && !proof.previous().isEmpty()) {
                for (var previous : proof.previous()) {
                    writeRdfcEntry(id, 14, previous, os);
                }
            }

            writeRdfcEntry(id, 16, proof.purpose(), os);
            writeRdfcEntry(id, 18, proof.verificationMethod(), os);

            return os.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static <T> void writeRdfcEntry(byte[] id, int index, String value, OutputStream os) throws IOException {
        if (value != null) {
            os.write(id);
            os.write(RDFC_TEMPLATE[index]);
            os.write(value.getBytes(StandardCharsets.UTF_8));
            os.write(RDFC_TEMPLATE[index + 1]);
            os.write('\n');
        }
    }

    private static <T> void writeRdfcEntry(byte[] id, int index, T value, Function<T, String> map, OutputStream os)
            throws IOException {
        if (value != null) {
            writeRdfcEntry(id, index, map.apply(value), os);
        }
    }

    private static <T> boolean writeJcsEntry(int index, String value, OutputStream os, boolean next)
            throws IOException {
        if (value != null) {
            if (next) {
                os.write(',');
            }
            os.write(JCS_TEMPLATE[index]);
            os.write(escape(value));
            os.write('\"');
            return true;
        }
        return next;
    }

    private static <T> boolean writeJcsEntry(int index, T value, Function<T, String> map, OutputStream os, boolean next)
            throws IOException {
        if (value != null) {
            if (next) {
                os.write(',');
            }
            os.write(JCS_TEMPLATE[index]);
            os.write(map.apply(value).getBytes(StandardCharsets.UTF_8));
            os.write('\"');
            return true;
        }
        return next;
    }

    /**
     * Escapes a string according to JCS (RFC 8785, Section 2.5) rules and encodes
     * the result directly to a UTF-8 byte array.
     *
     * @param value the string to escape
     * @return the escaped UTF-8 byte array
     * @throws IllegalArgumentException if invalid Unicode data (lone surrogates) is
     *                                  detected
     */
    private static byte[] escape(String value) {
        final int length = value.length();
        final ByteArrayOutputStream out = new ByteArrayOutputStream(Math.max(length, 16));
        final HexFormat hexFormat = HexFormat.of();

        for (int i = 0; i < length;) {
            int ch = value.codePointAt(i);
            switch (ch) {
            case '\t' -> {
                out.write('\\');
                out.write('t');
            }
            case '\b' -> {
                out.write('\\');
                out.write('b');
            }
            case '\n' -> {
                out.write('\\');
                out.write('n');
            }
            case '\r' -> {
                out.write('\\');
                out.write('r');
            }
            case '\f' -> {
                out.write('\\');
                out.write('f');
            }
            case '\"' -> {
                out.write('\\');
                out.write('"');
            }
            case '\\' -> {
                out.write('\\');
                out.write('\\');
            }
            default -> {
                if (ch <= 0x1F) {
                    out.write('\\');
                    out.write('u');
                    out.write('0');
                    out.write('0');
                    out.write(hexFormat.toHighHexDigit((byte) ch));
                    out.write(hexFormat.toLowHexDigit((byte) ch));

                } else if (ch >= 0xD800 && ch <= 0xDFFF) {
                    throw new IllegalArgumentException(
                            "RFC 8785 Compliance Error: Invalid Unicode data (lone surrogate) detected at index " + i);
                } else if (ch <= 0x7F) {
                    out.write(ch);

                } else if (ch <= 0x7FF) {
                    out.write(0xC0 | (ch >> 6));
                    out.write(0x80 | (ch & 0x3F));

                } else if (ch <= 0xFFFF) {
                    out.write(0xE0 | (ch >> 12));
                    out.write(0x80 | ((ch >> 6) & 0x3F));
                    out.write(0x80 | (ch & 0x3F));

                } else {
                    out.write(0xF0 | (ch >> 18));
                    out.write(0x80 | ((ch >> 12) & 0x3F));
                    out.write(0x80 | ((ch >> 6) & 0x3F));
                    out.write(0x80 | (ch & 0x3F));
                }
            }
            }
            i += Character.charCount(ch);
        }
        return out.toByteArray();
    }

    public static final class Builder {

        private final DataIntegrityProof proof;

        private Builder(DataIntegrityProof proof) {
            this.proof = proof;
        }

        public byte[] canonize(String c14n) {
            return canonize(DataIntegrityProof.getSignTemplate(c14n));
        }

        public byte[] canonize(Function<DataIntegrityProof, byte[]> canonizer) {

            Objects.requireNonNull(canonizer);

            proof.canonicalPayload = canonizer.apply(proof);
            return proof.canonicalPayload;
        }

        public DataIntegrityProof snapshot() {
            return proof;
        }

        public Builder created(Instant created) {
            proof.created = created != null
                    ? created.truncatedTo(ChronoUnit.SECONDS)
                    : null;
            return this;
        }

        public Builder expires(Instant expires) {
            proof.expires = expires != null
                    ? expires.truncatedTo(ChronoUnit.SECONDS)
                    : null;
            return this;
        }

        public Builder purpose(String purpose) {
            proof.purpose = purpose;
            return this;
        }

        public Builder verificationMethod(String verificationMethod) {
            proof.verificationMethod = verificationMethod;
            return this;
        }

        public Builder id(String id) {
            proof.id = id;
            return this;
        }

        public Builder challenge(String challenge) {
            proof.challenge = challenge;
            return this;
        }

        public Builder nonce(String nonce) {
            proof.nonce = nonce;
            return this;
        }

        public Builder previousProof(Collection<String> previousProof) {
            proof.previousProof = previousProof;
            return this;
        }

        public DataIntegrityProof build(Signature signature) {
            proof.signature = signature;
            return proof;
        }

        public Builder context(Collection<String> context) {
            proof.context = context;
            return this;
        }

        public CryptoSuite cryptosuite() {
            return proof.cryptosuite;
        }

        public String c14n() {
            return proof.cryptosuite != null
                    ? proof.cryptosuite.c14n()
                    : null;
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
                PayloadProcessor payload) {

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
                PayloadProcessor payload) {
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
                    di.id = statement[0];
                    break;
                case PREDICATE_CRYPTOSUITE:
                    di.cryptosuite = cryptosuites.get(statement[2]);
                    break;
                case PREDICATE_CREATED:
                    di.created = Instant.parse(statement[2]);
                    break;
                case PREDICATE_PROOF_PURPOSE:
                    di.purpose = statement[2];
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
