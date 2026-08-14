package com.apicatalog.di.proof.c14n;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.HexFormat;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.function.Function;
import java.util.stream.Stream;

import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.di.suite.CryptoSuite;

public final class StaticJCSCanonizer {

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

    /**
     * Builds the canonical {@link DataIntegrityProof} (JCS) for signing.
     *
     * @param proof
     * @return UTF-8 encoded JSON proof bytes
     */
    public static byte[] canonize(DataIntegrityProof proof) {
        try {
            var os = new ByteArrayOutputStream();
            os.write('{');

            var next = false;

            if (proof.context() != null && !proof.context().isEmpty()) {
                sequence(proof.context(), 11, os);
                next = true;
            }

            next = entry(1, proof.challenge(), os, next);
            next = entry(2, proof.created(), Instant::toString, os, next);
            next = entry(3, proof.cryptosuite(), CryptoSuite::id, os, next);

            if (proof.domains() != null) {
                if (proof.domains().size() > 1) {
                    next = collection(4, proof.domains(), os, next);

                } else if (proof.domains().size() == 1) {
                    next = entry(4, proof.domains().iterator().next(), os, next);
                }
            }

            next = entry(5, proof.expires(), Instant::toString, os, next);
            next = entry(6, proof.id(), os, next);
            next = entry(7, proof.nonce(), os, next);

            if (proof.previous() != null) {
                if (proof.previous().size() > 1) {
                    next = collection(8, proof.previous(), os, next);

                } else if (proof.previous().size() == 1) {
                    next = entry(8, proof.previous().iterator().next(), os, next);
                }
            }

            next = entry(9, proof.purpose(), os, next);

            if (next) {
                os.write(',');
            }

            os.write(JCS_TEMPLATE[0]); // type

            entry(10, proof.verificationMethod(), os, true);

            os.write('}');

            return os.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Builds the canonical {@link DataIntegrityProof} (JCS) for verification.
     *
     * @param proof
     * @return UTF-8 encoded JSON proof bytes
     */
    public static byte[] canonize(Map<String, ?> proof) {
        try {
            var os = new ByteArrayOutputStream();
            os.write('{');

            var next = false;

            if (proof.get("@context") instanceof Collection col) {
                next = collection(11, col, os, next);

            } else if (proof.get("@context") instanceof String value) {
                next = entry(11, value, os, next);
            }

            if (proof.get(DataIntegrityProof.KEY_CHALLENGE) instanceof String challenge) {
                next = entry(1, challenge, os, next);
            }

            if (proof.get(DataIntegrityProof.KEY_CREATED) instanceof String created) {
                next = entry(2, created, os, next);
            }

            if (proof.get(DataIntegrityProof.KEY_CRYPTOSUITE) instanceof String cryptosuite) {
                next = entry(3, cryptosuite, os, next);
            }

            if (proof.get(DataIntegrityProof.KEY_DOMAIN) instanceof Collection col) {
                next = collection(4, col, os, next);

            } else if (proof.get(DataIntegrityProof.KEY_DOMAIN) instanceof String value) {
                next = entry(4, value, os, next);
            }

            if (proof.get(DataIntegrityProof.KEY_EXPIRES) instanceof String expires) {
                next = entry(5, expires, os, next);
            }

            if (proof.get(DataIntegrityProof.KEY_ID) instanceof String id) {
                next = entry(6, id, os, next);
            }

            if (proof.get(DataIntegrityProof.KEY_NONCE) instanceof String nonce) {
                next = entry(7, nonce, os, next);
            }

            if (proof.get(DataIntegrityProof.KEY_PURPOSE) instanceof String purpose) {
                next = entry(9, purpose, os, next);
            }

            if (proof.get(DataIntegrityProof.KEY_PREVIOUS_PROOF) instanceof Collection col) {
                next = collection(8, col, os, next);

            } else if (proof.get(DataIntegrityProof.KEY_PREVIOUS_PROOF) instanceof String value) {
                next = entry(8, value, os, next);
            }

            if (next) {
                os.write(',');
            }

            os.write(JCS_TEMPLATE[0]); // type

            if (proof.get(DataIntegrityProof.KEY_VERIFICATION_METHOD) instanceof String vm) {
                entry(10, vm, os, true);
            }

            os.write('}');

            return os.toByteArray();

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void sequence(SequencedCollection<String> col, int index, OutputStream os)
            throws IOException {
        os.write(JCS_TEMPLATE[index]);
        if (col.size() == 1) {
            os.write('"');
            os.write(jcsEscape(col.getFirst()));
            os.write('"');
        } else {
            collection(col, os);
        }
    }

    private static boolean collection(int index, Collection<String> col, OutputStream os, boolean next)
            throws IOException {
        if (next) {
            os.write(',');
        }
        os.write(JCS_TEMPLATE[index]);
        collection(col, os);
        return true;
    }

    private static void collection(Collection<String> col, OutputStream os)
            throws IOException {
        os.write('[');
        boolean first = true;
        for (var element : col) {
            if (!first) {
                os.write(',');
            } else {
                first = false;
            }
            os.write('"');
            os.write(jcsEscape(element));
            os.write('"');
        }
        os.write(']');
    }

    private static boolean entry(int index, String value, OutputStream os, boolean next)
            throws IOException {
        if (value != null) {
            if (next) {
                os.write(',');
            }
            os.write(JCS_TEMPLATE[index]);
            os.write(jcsEscape(value));
            os.write('\"');
            return true;
        }
        return next;
    }

    private static <T> boolean entry(int index, T value, Function<T, String> map, OutputStream os, boolean next)
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
    private static byte[] jcsEscape(String value) {
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
