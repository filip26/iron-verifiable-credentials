package com.apicatalog.di.proof.c14n;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.trust.semantic.Graph;
import com.apicatalog.trust.semantic.SemanticModel.GraphCanonizer;

public final class StaticRDFC implements GraphCanonizer {

    private static final byte[][] RDFC_TEMPLATE = Stream.of(
            "_:c14n0",
            " <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://w3id.org/security#DataIntegrityProof> .\n",

            " <http://purl.org/dc/terms/created> \"",
            "\"^^<http://www.w3.org/2001/XMLSchema#dateTime> .\n",

            " <https://w3id.org/security#challenge> \"",
            "\" .\n",

            " <https://w3id.org/security#cryptosuite> \"",
            "\"^^<https://w3id.org/security#cryptosuiteString> .\n",

            " <https://w3id.org/security#domain> \"",
            "\" .\n",

            " <https://w3id.org/security#expiration> \"",
            "\"^^<http://www.w3.org/2001/XMLSchema#dateTime> .\n",

            " <https://w3id.org/security#nonce> \"",
            "\" .\n",

            " <https://w3id.org/security#previousProof> <",
            "> .\n",

            " <https://w3id.org/security#proofPurpose> <",
            "> .\n",

            " <https://w3id.org/security#verificationMethod> <",
            "> .\n")
            .map(i -> i.getBytes(StandardCharsets.UTF_8))
            .toArray(byte[][]::new);

    private String node;

    private String created;
    private String challenge;
    private String cryptosuite;
    private Collection<String> domains;
    private String expires;
    private String nonce;
    private Collection<String> previous;
    private String purpose;
    private String verificationMethod;

    public static final StaticRDFC newInstance() {
        return new StaticRDFC();
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
        case DataIntegrityProof.PREDICATE_CREATED:
            created = object;
            break;
        case DataIntegrityProof.PREDICATE_CHALLENGE:
            challenge = object;
            break;
        case DataIntegrityProof.PREDICATE_CRYPTOSUITE:
            cryptosuite = object;
            break;
        case DataIntegrityProof.PREDICATE_DOMAIN:
            if (domains == null) {
                domains = new ArrayList<>();
            }
            domains.add(object);
            break;            
        case DataIntegrityProof.PREDICATE_NONCE:
            nonce = object;
            break;
        case DataIntegrityProof.PREDICATE_EXPIRES:
            expires = object;
            break;
        case DataIntegrityProof.PREDICATE_PREVIOUS_PROOF:
            if (previous == null) {
                previous = new ArrayList<>();
            }
            previous.add(object);
            break;
        case DataIntegrityProof.PREDICATE_PROOF_PURPOSE:
            purpose = object;
            break;
        case DataIntegrityProof.PREDICATE_VERIFICATION_METHOD:
            verificationMethod = object;
            break;
        case Graph.PREDICATE_TYPE:
            if (!DataIntegrityProof.TYPE_URI.equals(object)) {
                throw new IllegalArgumentException();
            }
            break;
        default:
            throw new IllegalArgumentException("Unsupported predicate " + predicate);
        }

    }

    /**
     * Builds the deterministic N-Quads representation of a DataIntegrityProof for
     * RDF Dataset Canonicalization (RDFC).
     *
     * <p>
     * The returned value is UTF-8 encoded and suitable for verifying. The output
     * strictly follows N-Quads syntax and is deterministic for the supplied values.
     * </p>
     *
     * @param proof
     * @return UTF-8 encoded canonical N-Quads proof representation
     */
    @Override
    public byte[] canonize() {
        return canonize(
                node,
                created,
                challenge,
                cryptosuite,
                domains,
                expires,
                nonce,
                previous,
                purpose,
                verificationMethod);
    }

    /**
     * Builds the deterministic N-Quads representation of a DataIntegrityProof for
     * RDF Dataset Canonicalization (RDFC).
     *
     * <p>
     * The returned value is UTF-8 encoded and suitable for signing. The output
     * strictly follows N-Quads syntax and is deterministic for the supplied values.
     * </p>
     *
     * @param proof
     * @return UTF-8 encoded canonical N-Quads proof representation
     */
    public static byte[] canonize(DataIntegrityProof proof) {
        return canonize(
                proof.id(),
                proof.created() != null ? proof.created().toString() : null,
                proof.challenge(),
                proof.cryptosuite() != null ? proof.cryptosuite().id() : null,
                proof.domains(),
                proof.expires() != null ? proof.expires().toString() : null,
                proof.nonce(),
                proof.previous(),
                proof.purpose() != null ? "https://w3id.org/security#" + proof.purpose() : null,
                proof.verificationMethod());
    }

    public static byte[] canonize(
            String subject,
            String created,
            String challenge,
            String cryptosuite,
            Collection<String> domains,
            String expires,
            String nonce,
            Collection<String> previous,
            String purpose,
            String vm) {

        byte[] id = subject != null && !subject.startsWith("_:")
                ? ("<" + subject + ">").getBytes(StandardCharsets.UTF_8)
                : RDFC_TEMPLATE[0];

        try {
            var os = new ByteArrayOutputStream();

            if (created != null) {
                rdfcStatement(id, 2, created, os);
            }

            os.write(id);
            os.write(RDFC_TEMPLATE[1]); // type

            if (challenge != null) {
                rdfcStatement(id, 4, challenge, os);
            }
            if (cryptosuite != null) {
                rdfcStatement(id, 6, cryptosuite, os);
            }

            if (domains != null && !domains.isEmpty()) {
                domains.stream().sorted().forEach(domain -> rdfcStatement(id, 8, domain, os));
            }

            if (expires != null) {
                rdfcStatement(id, 10, expires, os);
            }

            if (nonce != null) {
                rdfcStatement(id, 12, nonce, os);
            }

            if (previous != null && !previous.isEmpty()) {
                previous.stream().sorted().forEach(el -> rdfcStatement(id, 14, el, os));
            }

            if (purpose != null) {
                rdfcStatement(id, 16, purpose, os);
            }

            if (vm != null) {
                rdfcStatement(id, 18, vm, os);
            }

            return os.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static <T> void rdfcStatement(byte[] id, int index, String value, OutputStream os) {
        try {
            os.write(id);
            os.write(RDFC_TEMPLATE[index]);
            os.write(value.getBytes(StandardCharsets.UTF_8));
            os.write(RDFC_TEMPLATE[index + 1]);

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
