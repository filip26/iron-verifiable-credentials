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

public final class StaticJCS {

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
     * Builds the canonical {@link DataIntegrityProof.Draft} (JCS) for signing.
     *
     * @param proofDraft
     * @return UTF-8 encoded JSON proof bytes
     */
    public static byte[] canonize(DataIntegrityProof.Draft proofDraft) {
        try {
            // proof.isContextArray() ||
            var os = new ByteArrayOutputStream();
            os.write('{');

            var next = false;

            if (proofDraft.context() instanceof Collection context) {
                next = collection(11, context, os, next);

            } else if (proofDraft.context() instanceof String context) {
                next = entry(11, context, os, next);
            }

            next = entry(1, proofDraft.challenge(), os, next);
            next = entry(2, proofDraft.created(), Instant::toString, os, next);
            next = entry(3, proofDraft.cryptosuite(), CryptoSuite::id, os, next);

            if (proofDraft.domains() != null && !proofDraft.domains().isEmpty()) {
                next = sequence(proofDraft.domains(), 4, os, next);
            }

            next = entry(5, proofDraft.expires(), Instant::toString, os, next);
            next = entry(6, proofDraft.id(), os, next);
            next = entry(7, proofDraft.nonce(), os, next);

            if (proofDraft.previous() != null && !proofDraft.previous().isEmpty()) {
                next = sequence(proofDraft.previous(), 8, os, next);
            }

            if (proofDraft.purpose() != null) {
                next = entry(9, proofDraft.purpose().key(), os, next);
            }

            if (next) {
                os.write(',');
            }

            os.write(JCS_TEMPLATE[0]); // type

            entry(10, proofDraft.verificationMethod(), os, true);

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

            } else if (proof.get(DataIntegrityProof.KEY_DOMAIN) instanceof String domain) {
                next = entry(4, domain, os, next);
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

    private static boolean sequence(SequencedCollection<String> col, int index, OutputStream os, boolean next)
            throws IOException {
        if (next) {
            os.write(',');
        }
        os.write(JCS_TEMPLATE[index]);
        if (col.size() == 1) {
            os.write('"');
            os.write(escape(col.getFirst()));
            os.write('"');
        } else {
            collection(col, os);
        }
        return true;
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
            os.write(escape(element));
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
            os.write(escape(value));
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
    private static byte[] escape(String value) {
        final int length = value.length();
        final ByteArrayOutputStream out = new ByteArrayOutputStream(Math.max(length, 16));

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
                    out.write(HexFormat.of().toHighHexDigit((byte) ch));
                    out.write(HexFormat.of().toLowHexDigit((byte) ch));

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
