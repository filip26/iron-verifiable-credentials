package com.apicatalog.di.proof;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import com.apicatalog.di.signature.ProofValue;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.tree.io.Tree;
import com.apicatalog.tree.io.TreeEmitter;
import com.apicatalog.trust.Signature;
import com.apicatalog.trust.data.Data;
import com.apicatalog.trust.model.GraphModel.C14nFactory;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.proof.ProofGraphReader;

public final class Ed25519Signature2020 implements Proof {

    public static String TYPE_NAME = "Ed25519Signature2020";
    public static String KEY_ALGORITHM = "Ed25519";
    public static String HASH_ALGORITHM = "SHA-256";
    public static String C14N = "RDFC";

    private static final String URI_TYPE_VALUE = "https://w3id.org/security#Ed25519Signature2020";
    private static final String URI_CREATED = "http://purl.org/dc/terms/created";
    private static final String URI_VERIFICATION_METHOD = "https://w3id.org/security#verificationMethod";
    private static final String URI_PURPOSE = "https://w3id.org/security#proofPurpose";
    private static final String URI_PROOF_VALUE = "https://w3id.org/security#proofValue";

    private Instant created;
    private String purpose;
    private String verificationMethod;
    private Signature signature;

    private byte[] canonicalPayload;
    private String c14n;

    private Ed25519Signature2020() {
    }

    public static void write(Ed25519Signature2020 proof, TreeEmitter emitter) {
        var writer = Tree.newPropertyTree(emitter)
                .beginMap()
                .entry("type", proof.type())
                .entry("created", proof.created, Instant::toString)
                .entry("verificationMethod", proof.verificationMethod)
                .entry("proofPurpose", proof.purpose);
        if (proof.signature != null) {
            writer.entry(
                    "proofValue",
                    proof.signature.toByteArray(),
                    Multibase.BASE_58_BTC::encode);
        }
        writer.endMap();
    }

    public static Ed25519Signature2020 generateProof(
            AsymmetricSigner signer,
            Ed25519Signature2020.Draft proofDraft,
            Data data) throws SignatureException {

        try {
            proofDraft.canonize();

            var signature = ProofValue.generateSignature(
                    signer,
                    Ed25519Signature2020.KEY_ALGORITHM,
                    MessageDigest.getInstance(HASH_ALGORITHM),
                    proofDraft.get(),
                    data);

            proofDraft.signature(signature);

            return proofDraft.get();

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Draft newDraft() {
        return new Draft(new Ed25519Signature2020(), List.of());
    }

    public static Draft newDraft(Map<String, Object> map) {

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
            case "created":
                proof.created = Instant.parse((String) entry.getValue());
                break;
            case "proofPurpose":
                proof.purpose = (String) entry.getValue();
                break;
            case "verificationMethod":
                proof.verificationMethod = (String) entry.getValue();
                break;
            }
        }

        return new Draft(proof, context);
    }

    public static final class Draft {

        private final Ed25519Signature2020 proof;
        private Collection<String> context;

        private Draft(Ed25519Signature2020 proof, Collection<String> context) {
            this.proof = proof;
            this.context = context;
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
            this.context = context;
            return this;
        }

        public Collection<String> context() {
            return context;
        }
    }

    public Instant created() {
        return created;
    }

    @Override
    public byte[] canonicalPayload() {
        return canonicalPayload;
    }

    @Override
    public String c14n() {
        return c14n;
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

    public static ProofGraphReader newReader(C14nFactory factory) {
        return new GraphReader(factory);
    }

    public static class GraphReader implements ProofGraphReader {

        private final C14nFactory factory;

        public GraphReader(C14nFactory factory) {
            this.factory = factory;
        }

        @Override
        public boolean isAccepted(Collection<String[]> proof) {
            for (var statement : proof) {
                if ("http://www.w3.org/1999/02/22-rdf-syntax-ns#type".equals(statement[1])
                        && URI_TYPE_VALUE.equals(statement[2])) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Proof read(Collection<String[]> proof, Function<Collection<String>, Data> data) {

            final var di = new Ed25519Signature2020();

            var canonizer = factory.newInstance();

            var consumer = canonizer.consumer();

            byte[] proofValue = null;

            for (var statement : proof) {
                switch (statement[1]) {
                case URI_CREATED:
                    di.created = Instant.parse(statement[2]);
                    break;
                case URI_PURPOSE:
                    di.purpose = statement[2];
                    break;
                case URI_VERIFICATION_METHOD:
                    di.verificationMethod = statement[2];
                    break;
                case URI_PROOF_VALUE:
                    proofValue = Multibase.BASE_58_BTC.decode(statement[2]);
                    break;
                }
                if (!URI_PROOF_VALUE.equals(statement[1])) { // TODO better
                    consumer.accept(statement[0], statement[1], statement[2], statement[3], statement[4], statement[5],
                            null);
                }
            }

            di.canonicalPayload = canonizer.canonize();

            if (proofValue != null) {

                try {
                    di.signature = ProofValue.newSignature(
                            Ed25519Signature2020.KEY_ALGORITHM,
                            MessageDigest.getInstance(HASH_ALGORITHM),
                            proofValue,
                            di,
                            data.apply(Set.of()));
                } catch (NoSuchAlgorithmException e) {
                    throw new IllegalStateException(e);
                }
            }

            return di;
        }
    }

    @Override
    public Collection<String> previous() {
        return Set.of();
    }
}
