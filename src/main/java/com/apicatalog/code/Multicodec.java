package com.apicatalog.code;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Multicodec {

    enum Type {
        Key,
    }

    enum Codec {
    
        Ed25519PublicKey(Type.Key,  new byte[]{0xe, 0xd}),
        Ed25519PrivateKey(Type.Key, new byte[]{0x1, 0x3, 0x0, 0x0});

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
        KEY_REGISTRY.put(Codec.Ed25519PublicKey.asInteger(), Codec.Ed25519PublicKey);
        KEY_REGISTRY.put(Codec.Ed25519PrivateKey.asInteger(), Codec.Ed25519PrivateKey);
    }

    public static Optional<Codec> codec(Type type, byte[] encoded) {

        switch (type) {
        case Key:

            Integer byte4 = new BigInteger(Arrays.copyOf(encoded, 4)).intValueExact();

            if (Multicodec.KEY_REGISTRY.containsKey(byte4)) {
                return Optional.of(KEY_REGISTRY.get(byte4));
            }

            Integer byte2 = new BigInteger(Arrays.copyOf(encoded, 2)).intValueExact();

            if (Multicodec.KEY_REGISTRY.containsKey(byte2)) {
                return Optional.of(KEY_REGISTRY.get(byte2));
            }
            break;

        default:
            break;
        }

        return Optional.empty();
    }

    public static byte[] encode(Codec codec, byte[] value) {
        
        final byte[] encoded = new byte[codec.length() + value.length];

        System.arraycopy(codec.code, 0, encoded, 0, codec.length());
        System.arraycopy(value, 0, encoded, 2, encoded.length);

        return encoded;
    }

    public static byte[] decode(Codec codec, byte[] encoded) {
        return Arrays.copyOfRange(encoded, codec.length(), encoded.length);
    }
}
