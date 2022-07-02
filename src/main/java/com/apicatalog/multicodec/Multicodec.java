package com.apicatalog.multicodec;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 *
 * see {@link https://github.com/multiformats/multicodec/blob/master/table.csv}
 *
 */
public final class Multicodec {

    public enum Type {
        Key,
        Multihash,
    }

    public enum Codec {
        Identity(Type.Multihash, new byte[]{(byte)0x00}),
        Ed25519PublicKey(Type.Key,  new byte[]{(byte)0xed, (byte)0x01}),
        Ed25519PrivateKey(Type.Key, new byte[]{(byte)0x13, (byte)0x00}),
        X25519PublicKey(Type.Key, new byte[]{(byte)0xec}),
        ;

        private final byte[] code;
        private final Type type;

        Codec(Type type, byte[] code) {
            this.type = type;
            this.code = code;
        }

        int length() {
            return code.length;
        }

        int asInteger() {
            return new BigInteger(code).intValue();
        }

        byte[] code() {
            return code;
        }

        Type type() {
            return type;
        }
    }

    static Map<Integer, Codec> KEY_REGISTRY = new HashMap<>();

    static {
        add(Codec.Identity);
        add(Codec.Ed25519PublicKey);
        add(Codec.Ed25519PrivateKey);
        add(Codec.X25519PublicKey);
    }

    static void add(final Codec codec) {
        KEY_REGISTRY.put(codec.asInteger(), codec);
    }

    static Optional<Codec> find(byte[] code) {
        return Optional.ofNullable(KEY_REGISTRY.get(new BigInteger(code).intValue()));
    }

    public static Optional<Codec> codec(Type type, final byte[] encoded) {

        switch (type) {
        case Key:
            return Optional.of(find(Arrays.copyOf(encoded, 4))      // try first 4 bytes
                    .orElseGet(() -> find(Arrays.copyOf(encoded, 2))    // try first 2 bytes
                    .orElseGet(() -> find(Arrays.copyOf(encoded, 1))    // try the first byte
                    .orElse(null)
                    )));

        default:
            break;
        }

        throw new IllegalArgumentException("Unsupported type [" + type + "].");
    }

    public static byte[] encode(Codec codec, byte[] value) {

        final byte[] encoded = new byte[codec.length() + value.length];

        System.arraycopy(codec.code, 0, encoded, 0, codec.length());
        System.arraycopy(value, 0, encoded, codec.length(), value.length);

        return encoded;
    }

    public static byte[] decode(final Codec codec, final byte[] encoded) {
        return Arrays.copyOfRange(encoded, codec.length(), encoded.length);
    }

    public static byte[] decode(final Type type, final byte[] encoded) throws IllegalArgumentException {
        return codec(type, encoded)
                .map(codec -> decode(codec, encoded))
                .orElseThrow(() -> new IllegalArgumentException("Unsupported multicode encoding"));
    }
}
