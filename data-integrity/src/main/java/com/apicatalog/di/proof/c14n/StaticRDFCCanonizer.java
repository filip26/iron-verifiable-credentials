package com.apicatalog.di.proof.c14n;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.di.suite.CryptoSuite;
import com.apicatalog.trust.semantic.SemanticModel.GraphCanonizer;
import com.apicatalog.trust.semantic.SemanticModel.QuadConsumer;

public final class StaticRDFCCanonizer implements GraphCanonizer {

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

    public static final StaticRDFCCanonizer newInstance() {
        return new StaticRDFCCanonizer();
    }

    @Override
    public void accept(String subject, String predicate, String object, String datatype, String language,
            String direction, String graph) {
        // TODO Auto-generated method stub

    }

    @Override
    public byte[] canonize() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void canonize(QuadConsumer consumer) {
        // TODO Auto-generated method stub

    }

    @Override
    public Map<String, String> labels() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String toNQuad(String subject, String predicate, String object, String datatype, String language,
            String direction, String graph) {
        // TODO Auto-generated method stub
        return null;
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
    public static byte[] canonize(DataIntegrityProof proof) {

        byte[] id = proof.id() != null
                ? ("<" + proof.id() + ">").getBytes(StandardCharsets.UTF_8)
                : RDFC_TEMPLATE[0];

        try {
            var os = new ByteArrayOutputStream();

            rdfcStatement(id, 2, proof.created(), Instant::toString, os);

            os.write(id);
            os.write(RDFC_TEMPLATE[1]);
            os.write('\n');

            rdfcStatement(id, 4, proof.challenge(), os);
            rdfcStatement(id, 6, proof.cryptosuite(), CryptoSuite::id, os);

            if (proof.domains() != null && !proof.domains().isEmpty()) {
                for (var domain : proof.domains()) {
                    rdfcStatement(id, 8, domain, os);
                }
            }

            rdfcStatement(id, 10, proof.expires(), Instant::toString, os);
            rdfcStatement(id, 12, proof.nonce(), os);

            if (proof.previous() != null && !proof.previous().isEmpty()) {
                for (var previous : proof.previous()) {
                    rdfcStatement(id, 14, previous, os);
                }
            }

            rdfcStatement(id, 16, proof.purpose(), os);
            rdfcStatement(id, 18, proof.verificationMethod(), os);

            return os.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static <T> void rdfcStatement(byte[] id, int index, String value, OutputStream os) throws IOException {
        if (value != null) {
            os.write(id);
            os.write(RDFC_TEMPLATE[index]);
            os.write(value.getBytes(StandardCharsets.UTF_8));
            os.write(RDFC_TEMPLATE[index + 1]);
            os.write('\n');
        }
    }

    private static <T> void rdfcStatement(byte[] id, int index, T value, Function<T, String> map, OutputStream os)
            throws IOException {
        if (value != null) {
            rdfcStatement(id, index, map.apply(value), os);
        }
    }
}
