package com.apicatalog.di.proof;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.function.Function;
import java.util.stream.Stream;

import com.apicatalog.di.suite.CryptoSuite;
import com.apicatalog.trust.model.Model;

final class DIProofC14NTemplates {

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

    static Function<DataIntegrityProof, byte[]> getSignTemplate(String c14n) {
        return switch (c14n) {
        case Model.C14N_JCS -> DIProofC14NTemplates::jcs;
        case Model.C14N_RDFC -> DIProofC14NTemplates::rdfc;
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
}
