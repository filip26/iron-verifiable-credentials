package com.apicatalog.di.proof;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
import com.apicatalog.trust.semantic.GraphProofReader;
import com.apicatalog.trust.semantic.SemanticModel;
import com.apicatalog.trust.signature.Signature;

public final class Ed25519Signature2020 implements Proof {

    public static final String TYPE_URI = "https://w3id.org/security#Ed25519Signature2020";
    public static String TYPE_NAME = "Ed25519Signature2020";

    public static String SIGNATURE_ALGORITHM = "Ed25519";
    public static String HASH_ALGORITHM = "SHA-256";
    public static String C14N = "RDFC";

    private static final String PREDICATE_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
    private static final String PREDICATE_CREATED = "http://purl.org/dc/terms/created";
    private static final String PREDICATE_VERIFICATION_METHOD = "https://w3id.org/security#verificationMethod";
    private static final String PREDICATE_PROOF_PURPOSE = "https://w3id.org/security#proofPurpose";
    private static final String PREDICATE_PROOF_VALUE = "https://w3id.org/security#proofValue";

    private static final String KEY_TYPE = "type";
    private static final String KEY_CREATED = "created";
    private static final String KEY_VERIFICATION_METHOD = "verificationMethod";
    private static final String KEY_PURPOSE = "proofPurpose";
    private static final String KEY_PROOF_VALUE = "proofValue";

    private Collection<String> context;

    private Instant created;
    private String purpose;
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
                .entry(KEY_PURPOSE, proof.purpose);
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

    public static Draft newInstance() {
        return new Draft(new Ed25519Signature2020(), List.of());
    }

    public static Draft newInstance(Map<String, Object> map) {

        var proof = new Ed25519Signature2020();
        Collection<String> context = List.of();

        for (var entry : map.entrySet()) {
            switch (entry.getKey()) {
            case "@context":
                if (entry.getValue() instanceof Collection<?> col) {
                    context = col.stream().map(String.class::cast).toList();

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
                proof.purpose = (String) entry.getValue();
                break;
            case KEY_VERIFICATION_METHOD:
                proof.verificationMethod = (String) entry.getValue();
                break;
            }
        }

        return new Draft(proof, context);
    }

    public static final class Draft {

        private final Ed25519Signature2020 proof;

        private Draft(Ed25519Signature2020 proof, Collection<String> context) {
            this.proof = proof;
            this.proof.context = context;
        }

        public byte[] canonize() {
            proof.canonicalPayload = Ed25519Signature2020.canonize(proof);
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

        public Draft purpose(String purpose) {
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

        public Draft context(Collection<String> context) {
            proof.context = context;
            return this;
        }

        public Collection<String> context() {
            return proof.context;
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
//
//    public void validate(Map<String, Object> params) throws DocumentError {
//        if (created == null) {
//            throw new DocumentError(ErrorType.Missing, "Created");
//        }
//        if (method == null) {
//            throw new DocumentError(ErrorType.Missing, "VerificationMethod");
//        }
//        if (purpose == null) {
//            throw new DocumentError(ErrorType.Missing, "ProofPurpose");
//        }
//        if (value == null || ((SolidProofValue) value).toByteArray().length == 0) {
//            throw new DocumentError(ErrorType.Missing, "ProofValue");
//        }
//        // proof value must be 64 bytes
//        if (((SolidProofValue) value).toByteArray().length != 64) {
//            throw new DocumentError(ErrorType.Invalid, "ProofValue");
//        }
//
//        if (params != null) {
//            assertEquals(params, DataIntegrityVocab.PURPOSE, purpose.toString()); // TODO compare as URI, expect URI in params
//            assertEquals(params, DataIntegrityVocab.CHALLENGE, challenge);
//            assertEquals(params, DataIntegrityVocab.DOMAIN, domain);
//        }
//    }
//
//    protected static void assertEquals(Map<String, Object> params, Term name, String param) throws DocumentError {
//
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
//    
    private final static byte[][] RDFC_TEMPLATE = Stream.of(
            "_:c14n0 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://w3id.org/security#Ed25519Signature2020> .",

            "_:c14n0 <http://purl.org/dc/terms/created> \"",
            "\"^^<http://www.w3.org/2001/XMLSchema#dateTime> .",

            "_:c14n0 <https://w3id.org/security#proofPurpose> <https://w3id.org/security#",
            "> .",

            "_:c14n0 <https://w3id.org/security#verificationMethod> <",
            "> .")
            .map(i -> i.getBytes(StandardCharsets.UTF_8))
            .toArray(byte[][]::new);

    private static byte[] canonize(Ed25519Signature2020 proof) {
        try {
            var os = new ByteArrayOutputStream();
            if (proof.created != null) {
                os.write(RDFC_TEMPLATE[1]);
                os.write(proof.created.toString().getBytes(StandardCharsets.UTF_8));
                os.write(RDFC_TEMPLATE[2]);
                os.write('\n');
            }

            os.write(RDFC_TEMPLATE[0]);
            os.write('\n');

            if (proof.purpose != null) {
                os.write(RDFC_TEMPLATE[3]);
                os.write(proof.purpose.getBytes(StandardCharsets.UTF_8));
                os.write(RDFC_TEMPLATE[4]);
                os.write('\n');
            }

            if (proof.verificationMethod != null) {
                os.write(RDFC_TEMPLATE[5]);
                os.write(proof.verificationMethod.getBytes(StandardCharsets.UTF_8));
                os.write(RDFC_TEMPLATE[6]);
                os.write('\n');
            }

            return os.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static GraphProofReader newReader() {
        return new GraphReader();
    }

    public static class GraphReader implements GraphProofReader {

        @Override
        public boolean isAccepted(Collection<String[]> proof) {
            return true;
        }

        @Override
        public Proof read(
                Collection<String[]> proof,
                SemanticModel model,
                PayloadGenerator payload) {

            final var di = new Ed25519Signature2020();

            var canonizer = model.newCanonizer();

            var consumer = canonizer.consumer();

            byte[] proofValue = null;

            for (var statement : proof) {
                switch (statement[1]) {
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
                    proofValue = Multibase.BASE_58_BTC.decode(statement[2]);
                    break;
                case PREDICATE_TYPE:
                    if (!TYPE_URI.equals(statement[2])) {
                        throw new IllegalArgumentException(
                                """
                                Proof type mismatch; %s for proof %s.
                                """.formatted(statement[2], TYPE_URI));
                    }
                    break;
                default:
                    throw new IllegalArgumentException(
                            """
                            An unsupported predicate %s for proof %s.
                            """.formatted(statement[1], TYPE_URI));

                }
                if (!PREDICATE_PROOF_VALUE.equals(statement[1])) {
                    consumer.accept(
                            statement[0], // subject
                            statement[1], // predicate
                            statement[2], // object
                            statement[3], // datatype
                            statement[4], // language
                            statement[5], // direction
                            null);
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
