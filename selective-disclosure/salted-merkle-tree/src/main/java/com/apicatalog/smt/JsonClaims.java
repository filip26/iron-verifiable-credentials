package com.apicatalog.smt;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

public class JsonClaims {

    static byte[] JCS_NULL = "null".getBytes(StandardCharsets.UTF_8);
    static byte[] JCS_TRUE = "true".getBytes(StandardCharsets.UTF_8);
    static byte[] JCS_FALSE = "false".getBytes(StandardCharsets.UTF_8);

    final Map<String, byte[][]> claims;

    protected JsonClaims(Map<String, byte[][]> claims) {
        this.claims = claims;
    }

    public static JsonClaims from(Map<String, ?> document) {
        var claims = new TreeMap<String, byte[][]>();

        flatten("", document, claims, Set.of());

        return new JsonClaims(claims);
    }

    public static JsonClaims from(Map<String, ?> document, Collection<String> selected) {
        var claims = new TreeMap<String, byte[][]>();

        flatten("", document, claims, selected);

        return new JsonClaims(claims);
    }

    static void flatten(String path, Object node, Map<String, byte[][]> claims, Collection<String> selected) {

        if (selected.contains(path)) {
            claims.put(path, null);
            return;
        }

        switch (node) {
        case Collection<?> col -> {
            var it = col.iterator();
            int i = 0;
            while (it.hasNext()) {
                flatten(path + "/" + i, it.next(), claims, selected);
                i++;
            }
        }
        case Map<?, ?> map -> {
            for (var entry : map.entrySet()) {
                flatten(path + "/" + jsonPointer((String) entry.getKey()), entry.getValue(), claims, selected);
            }
        }
        case Number number -> add(path, canonizeNumber(number), claims);
        case String string -> add(path, canonizeString(string), claims);
        case null -> add(path, JCS_NULL, claims);
        case Boolean bool -> add(path, bool ? JCS_TRUE : JCS_FALSE, claims);
        default -> {
            throw new IllegalArgumentException();
        }
        }
    }

    static void add(String path, byte[] value, Map<String, byte[][]> claims) {
        claims.put(path, claim(path, value));
    }

    public void add(String path, byte[] value) {
        claims.put(path, claim(path, value));
    }

    public static byte[][] claim(String path, byte[] value) {
        return new byte[][] {
                path.getBytes(StandardCharsets.UTF_8),
                new byte[] { 0x00 },
                value
        };
    }

    public static String jsonPointer(String token) {
        return token.replace("~", "~0").replace("/", "~1");
    }

    /**
     * Canonicalizes as JSON number according to JCS (RFC 8785).
     *
     * Numbers are strictly restricted to IEEE 754 double-precision format.
     * Serialization matches ECMAScript Number.prototype.toString() rules exactly.
     *
     * @param number the BigDecimal to canonicalize
     * @return the canonical byte array representation of the number
     * @throws IllegalArgumentException if the number overflows IEEE limits
     */
    public static byte[] canonizeNumber(final Number number) {
        double d = number.doubleValue();

        if (Double.isInfinite(d) || Double.isNaN(d)) {
            throw new IllegalArgumentException("RFC 8785 Compliance Error: Number exceeds IEEE 754 limits");
        }

        if (d == 0.0) {
            return new byte[] { '0' };
        }

        var string = Double.toString(d);

        var isNegative = d < 0.0;
        var start = isNegative ? 1 : 0;

        var eIndex = string.indexOf('E', start);
        if (eIndex == -1) {
            if (string.endsWith(".0")) {
                int length = string.length() - 2;
                byte[] res = new byte[length];
                for (int i = 0; i < length; i++) {
                    res[i] = (byte) string.charAt(i);
                }
                return res;
            }
            return string.getBytes(StandardCharsets.UTF_8);
        }

        int dot = string.indexOf('.', start);
        char firstDigit = string.charAt(start);

        int fracStart = (dot < 0) ? start + 1 : dot + 1;
        int fracEnd = eIndex;
        while (fracEnd > fracStart && string.charAt(fracEnd - 1) == '0') {
            fracEnd--;
        }
        int fracLen = fracEnd - fracStart;

        int exponent = Integer.parseInt(string, eIndex + 1, string.length(), 10);

        byte[] buf = new byte[128];
        int pos = 0;

        if (isNegative) {
            buf[pos++] = '-';
        }

        if (exponent >= 21 || exponent <= -7) {
            buf[pos++] = (byte) firstDigit;
            if (fracLen > 0) {
                buf[pos++] = '.';
                for (int i = fracStart; i < fracEnd; i++) {
                    buf[pos++] = (byte) string.charAt(i);
                }
            }
            buf[pos++] = 'e';
            if (exponent > 0) {
                buf[pos++] = '+';
            }
            String expStr = Integer.toString(exponent);
            for (int i = 0; i < expStr.length(); i++) {
                buf[pos++] = (byte) expStr.charAt(i);
            }
            return Arrays.copyOf(buf, pos);
        }

        if (exponent == 0) {
            buf[pos++] = (byte) firstDigit;
            if (fracLen > 0) {
                buf[pos++] = '.';
                for (int i = fracStart; i < fracEnd; i++) {
                    buf[pos++] = (byte) string.charAt(i);
                }
            }
        } else if (exponent > 0) {
            buf[pos++] = (byte) firstDigit;
            if (exponent >= fracLen) {
                for (int i = fracStart; i < fracEnd; i++) {
                    buf[pos++] = (byte) string.charAt(i);
                }
                int remainingZeros = exponent - fracLen;
                for (int i = 0; i < remainingZeros; i++) {
                    buf[pos++] = '0';
                }
            } else {
                for (int i = 0; i < exponent; i++) {
                    buf[pos++] = (byte) string.charAt(fracStart + i);
                }
                buf[pos++] = '.';
                for (int i = exponent; i < fracLen; i++) {
                    buf[pos++] = (byte) string.charAt(fracStart + i);
                }
            }
        } else {
            buf[pos++] = '0';
            buf[pos++] = '.';
            int leadingZeros = (-exponent) - 1;
            for (int i = 0; i < leadingZeros; i++) {
                buf[pos++] = '0';
            }
            buf[pos++] = (byte) firstDigit;
            for (int i = fracStart; i < fracEnd; i++) {
                buf[pos++] = (byte) string.charAt(i);
            }
        }

        return Arrays.copyOf(buf, pos);
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
    public static byte[] canonizeString(String value) {
        final int length = value.length();
        final ByteArrayOutputStream out = new ByteArrayOutputStream(Math.max(length, 16));
        final HexFormat hexFormat = HexFormat.of();

        out.write('"');

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
        out.write('"');
        return out.toByteArray();
    }

    public int size() {
        return claims.size();
    }

    public Collection<Entry<String, byte[][]>> entries() {
        return claims.entrySet();
    }

    public List<byte[][]> values() {
        return List.copyOf(claims.values());
    }

    public static String toString(byte[][] claim) {

        if (claim == null) {
            return Objects.toString(claim);
        }

        return "Claim(UTF8("
                + new String(claim[0], StandardCharsets.UTF_8)
                + ") || 0x00 || JCS("
                + new String(claim[1], StandardCharsets.UTF_8)
                + "))";

//        var index = separator(claim);

//        return "Claim(UTF8("
//                + new String(claim, 0, index, StandardCharsets.UTF_8)
//                + ") || 0x00 || JCS("
//                + new String(claim, index + 1, claim.length - index - 1, StandardCharsets.UTF_8)
//                + "))";
    }

    public static int separator(byte[] claim) {
        int index = -1;
        for (int i = 0; i < claim.length; i++) {
            if (claim[i] == 0) {
                index = i;
                break;
            }
        }
        return index;
    }

}
