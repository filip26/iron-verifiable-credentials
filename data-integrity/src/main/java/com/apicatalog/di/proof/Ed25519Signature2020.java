package com.apicatalog.di.proof;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.apicatalog.di.signature.ProofValue;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.security.Digestor;
import com.apicatalog.tree.io.Tree;
import com.apicatalog.tree.io.TreeEmitter;
import com.apicatalog.tree.io.java.NativeComposer;
import com.apicatalog.trust.payload.DigestiblePayload;
import com.apicatalog.trust.payload.PayloadGenerator;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.semantic.Graph;
import com.apicatalog.trust.semantic.GraphProofMapper;
import com.apicatalog.trust.semantic.SemanticModel;
import com.apicatalog.trust.semantic.SemanticModel.GraphCanonizer;
import com.apicatalog.trust.signature.Signature;

public final class Ed25519Signature2020 implements Proof {

    public static final String CONTEXT_URI = "https://w3id.org/security/suites/ed25519-2020/v1";

    public static final String TYPE_URI = "https://w3id.org/security#Ed25519Signature2020";
    public static final String TYPE_NAME = "Ed25519Signature2020";

    public static final String SIGNATURE_ALGORITHM = "Ed25519";
    public static final String HASH_ALGORITHM = "SHA-256";
    public static final String C14N = "RDFC";

    public static final int SIGNATURE_LENGTH = 64;
    public static final int PUBLIC_KEY_SIZE = 32;

    private static final String PREDICATE_CREATED = "http://purl.org/dc/terms/created";
    private static final String PREDICATE_VERIFICATION_METHOD = "https://w3id.org/security#verificationMethod";
    private static final String PREDICATE_PROOF_PURPOSE = "https://w3id.org/security#proofPurpose";
    private static final String PREDICATE_PROOF_VALUE = "https://w3id.org/security#proofValue";

    private static final String KEY_TYPE = "type";
    private static final String KEY_CREATED = "created";
    private static final String KEY_VERIFICATION_METHOD = "verificationMethod";
    private static final String KEY_PURPOSE = "proofPurpose";
    private static final String KEY_PROOF_VALUE = "proofValue";

    private Collection<?> context;

    private Instant created;
    private Purpose purpose;
    private String verificationMethod;
    private Signature signature;

    private byte[] canonicalPayload;
    private String c14n;

    private Ed25519Signature2020() {
    }

    public static Map<String, ?> compact(Ed25519Signature2020 proof) {
        var composer = new NativeComposer<Map<String, ? extends Object>>();
        compact(proof, composer);
        return composer.compose();
    }

    public static void compact(Ed25519Signature2020 proof, TreeEmitter emitter) {
        var writer = Tree.newPropertyTree(emitter)
                .beginMap()
                .entry(KEY_TYPE, proof.type())
                .entry(KEY_CREATED, proof.created, Instant::toString)
                .entry(KEY_VERIFICATION_METHOD, proof.verificationMethod)
                .entry(KEY_PURPOSE, proof.purpose != null ? proof.purpose.key() : null);
        if (proof.signature != null) {
            writer.entry(
                    KEY_PROOF_VALUE,
                    proof.signature.toByteArray(),
                    Multibase.BASE_58_BTC::encode);
        }
        writer.endMap();
    }

    public static Ed25519Signature2020 generateProof(
            AsymmetricSigner signer,
            Digestor.Factory digestFactory,
            Ed25519Signature2020.Draft proofDraft,
            DigestiblePayload payload) throws SignatureException {

        proofDraft.canonize();

        var digestor = digestFactory.newDigestor(HASH_ALGORITHM);

        var signature = ProofValue.generateSignature(
                Ed25519Signature2020.SIGNATURE_ALGORITHM,
                Ed25519Signature2020.HASH_ALGORITHM,
                signer,
                digestor,
                proofDraft.get(),
                payload);

        proofDraft.signature(signature);

        return proofDraft.get();
    }

    public static Draft newDraft() {
        return new Draft(new Ed25519Signature2020(), List.of());
    }

    public static Draft newDraft(Map<String, ?> map) {

        var proof = new Ed25519Signature2020();
        Collection<?> context = List.of();

        for (var entry : map.entrySet()) {
            switch (entry.getKey()) {
            case "@context":
                if (entry.getValue() instanceof Collection<?> col) {
                    context = col;

                } else if (entry.getValue() instanceof String uri) {
                    context = List.of(uri);

                } else {
                    throw new IllegalArgumentException();
                }
                break;
            case KEY_CREATED:
                proof.created = Instant.parse((String) entry.getValue());
                break;
            case KEY_PURPOSE:
                proof.purpose = Purpose.from((String) entry.getValue());
                break;
            case KEY_VERIFICATION_METHOD:
                proof.verificationMethod = (String) entry.getValue();
                break;
            }
        }

        // setup default context
        if (context.isEmpty()) {
            context = Set.of(Ed25519Signature2020.CONTEXT_URI);

        } else if (!context.contains(Ed25519Signature2020.CONTEXT_URI)) {
            var tmp = new LinkedHashSet<>(context.size());
            tmp.addAll(context);
            tmp.add(Ed25519Signature2020.CONTEXT_URI);
            context = tmp;
        }

        return new Draft(proof, context);
    }

    public static final class Draft {

        private final Ed25519Signature2020 proof;

        private Draft(Ed25519Signature2020 proof, Collection<?> context) {
            this.proof = proof;
            this.proof.context = context;
        }

        public byte[] canonize() {
            proof.canonicalPayload = Ed25519Signature2020.StaticRDFC.canonize(proof);
            return proof.canonicalPayload;
        }

        public Ed25519Signature2020 get() {
            return proof;
        }

        public Draft created(Instant created) {
            proof.created = created != null
                    ? created.truncatedTo(ChronoUnit.SECONDS)
                    : null;
            return this;
        }

        public Draft purpose(Purpose purpose) {
            proof.purpose = purpose;
            return this;
        }

        public Draft verificationMethod(String verificationMethod) {
            proof.verificationMethod = verificationMethod;
            return this;
        }

        public Draft id(String id) {
            proof.verificationMethod = id;
            return this;
        }

        public Draft signature(Signature signature) {
            proof.signature = signature;
            return this;
        }

        public Draft context(Collection<?> context) {
            proof.context = context;
            return this;
        }

        public Collection<?> context() {
            return proof.context;
        }

        public boolean hasRequired() {
            return proof.hasRequired();
        }

        public Purpose purpose() {
            return proof.purpose;
        }

        public Instant created() {
            return proof.created;
        }
    }

    public Instant created() {
        return created;
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
        return c14n;
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
    public Proof.Purpose purpose() {
        return purpose;
    }

    /**
     * The JSON-LD context used to process the proof. Optional.
     * 
     * @return a collection of strings representing the JSON-LD context URIs, or
     *         {@code null} if not present
     */
    public Collection<?> context() {
        return context;
    }

    public static StaticRDFC newStaticRDFC() {
        return new StaticRDFC();
    }

    @Override
    public boolean hasRequired() {
        return created != null
                && verificationMethod != null
                && purpose != null;
    }

    @Override
    public boolean isExpired() {
        return false;
    }

    public static class StaticRDFC implements GraphCanonizer {

        private final static byte[][] RDFC_TEMPLATE = Stream.of(
                "_:c14n0 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://w3id.org/security#Ed25519Signature2020> .",

                "_:c14n0 <http://purl.org/dc/terms/created> \"",
                "\"^^<http://www.w3.org/2001/XMLSchema#dateTime> .",

                "_:c14n0 <https://w3id.org/security#proofPurpose> <",
                "> .",

                "_:c14n0 <https://w3id.org/security#verificationMethod> <",
                "> .")
                .map(i -> i.getBytes(StandardCharsets.UTF_8))
                .toArray(byte[][]::new);

        private String node;

        private String created;
        private String purpose;
        private String verificationMethod;

        public static byte[] canonize(Ed25519Signature2020 proof) {
            return canonize(
                    proof.created != null ? proof.created.toString() : null,
                    proof.purpose != null ? proof.purpose.uri() : null,
                    proof.verificationMethod);
        }

        @Override
        public byte[] canonize() {
            return canonize(
                    created,
                    purpose,
                    verificationMethod);
        }

        @Override
        public void accept(
                String subject,
                String predicate,
                String object,
                String datatype,
                String language,
                String direction,
                String graph) {

            if (node == null) {
                node = subject;

            } else if (!node.equals(subject)) {
                throw new IllegalArgumentException();
            }

            switch (predicate) {
            case PREDICATE_CREATED:
                created = object;
                break;
            case PREDICATE_PROOF_PURPOSE:
                purpose = object;
                break;
            case PREDICATE_VERIFICATION_METHOD:
                verificationMethod = object;
                break;
            case Graph.PREDICATE_TYPE:
                if (!TYPE_URI.equals(object)) {
                    throw new IllegalArgumentException();
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported predicate " + predicate);
            }

        }

        private static byte[] canonize(
                String created,
                String purpose,
                String verificationMethod) {
            try {
                var os = new ByteArrayOutputStream();
                if (created != null) {
                    os.write(RDFC_TEMPLATE[1]);
                    os.write(created.getBytes(StandardCharsets.UTF_8));
                    os.write(RDFC_TEMPLATE[2]);
                    os.write('\n');
                }

                os.write(RDFC_TEMPLATE[0]);
                os.write('\n');

                if (purpose != null) {
                    os.write(RDFC_TEMPLATE[3]);
                    os.write(purpose.getBytes(StandardCharsets.UTF_8));
                    os.write(RDFC_TEMPLATE[4]);
                    os.write('\n');
                }

                if (verificationMethod != null) {
                    os.write(RDFC_TEMPLATE[5]);
                    os.write(verificationMethod.getBytes(StandardCharsets.UTF_8));
                    os.write(RDFC_TEMPLATE[6]);
                    os.write('\n');
                }

                return os.toByteArray();

            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    public static class GraphMapper implements GraphProofMapper {

        private final Supplier<GraphCanonizer> canonizerFactory;

        public GraphMapper(Supplier<GraphCanonizer> canonizerFactory) {
            this.canonizerFactory = canonizerFactory;
        }

        @Override
        public boolean accepts(Graph.Node proofNode) {
            return proofNode.type().size() == 1
                    && TYPE_URI.equals(proofNode.type().getFirst());
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

            final var proofType = proofNode.type().getFirst();

            if (!TYPE_URI.equals(proofType)) {
                throw new IllegalArgumentException(
                        """
                        An unexpected proof type has been detected %s, expected %s.
                        """.formatted(proofType, TYPE_URI));
            }

            final var di = new Ed25519Signature2020();

            var canonizer = canonizerFactory.get();

            byte[] proofValue = null;

            for (var statement : proofNode.statements()) {

                boolean canonizeStatement = true;

                switch (statement.predicate()) {
                case PREDICATE_CREATED:
                    // TODO datatype
                    di.created = Instant.parse(statement.object());
                    break;
                case PREDICATE_PROOF_PURPOSE:
                    // TODO type
                    di.purpose = Purpose.from(statement.object());
                    break;
                case PREDICATE_VERIFICATION_METHOD:
                    // TODO type
                    di.verificationMethod = statement.object();
                    break;
                case PREDICATE_PROOF_VALUE:
                    // TODO type
                    canonizeStatement = false;
                    proofValue = Multibase.BASE_58_BTC.decode(statement.object());
                    break;

                case Graph.PREDICATE_TYPE:
                    break;

                default:
                    throw new IllegalArgumentException(
                            """
                            An unsupported predicate %s for proof %s.
                            """.formatted(statement.predicate(), TYPE_URI));
                }

                if (canonizeStatement) {
                    canonizer.accept(
                            proofNode.id(),
                            statement.predicate(),
                            statement.object(),
                            statement.datatype(),
                            statement.language(),
                            statement.direction(),
                            null // graph
                    );
                }
            }

            di.canonicalPayload = canonizer.canonize();

            if (proofValue != null) {
                di.signature = ProofValue.newInstance(
                        SIGNATURE_ALGORITHM,
                        HASH_ALGORITHM,
                        proofValue,
                        di,
                        payload);
            }

            return di;
        }
    }
}
