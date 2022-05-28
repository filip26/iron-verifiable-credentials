package com.apicatalog.code;

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
public class Multicodec {

    public enum Type {
        Key,
    }

    public enum Codec {
    
        Ed25519PublicKey(Type.Key,  new byte[]{(byte)0xed, (byte)0x01}),
        Ed25519PrivateKey(Type.Key, new byte[]{(byte)0x13, (byte)0x00}),
        
//        UnknownPrivateKey(Type.Key, new byte[]{(byte)0x80, (byte)0x26, (byte)0x9b, (byte)0x93}) //FIXME ?!?!?!?
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
        KEY_REGISTRY.put(Codec.Ed25519PublicKey.asInteger(), Codec.Ed25519PublicKey);
        KEY_REGISTRY.put(Codec.Ed25519PrivateKey.asInteger(), Codec.Ed25519PrivateKey);
    }

    public static Optional<Codec> codec(Type type, final byte[] encoded) {

        switch (type) {
        case Key:

            Integer byte4 = new BigInteger(Arrays.copyOf(encoded, 4)).intValue();            
            if (Multicodec.KEY_REGISTRY.containsKey(byte4)) {
                return Optional.of(KEY_REGISTRY.get(byte4));
            }

            
            Integer byte2 = new BigInteger(Arrays.copyOf(encoded, 2)).intValue();
            
            if (Multicodec.KEY_REGISTRY.containsKey(byte2)) {
                return Optional.of(KEY_REGISTRY.get(byte2));
            }

            Integer byte1 = (int) encoded[0];

            if (Multicodec.KEY_REGISTRY.containsKey(byte1)) {
                return Optional.of(KEY_REGISTRY.get(byte1));
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
        System.arraycopy(value, 0, encoded, codec.length(), value.length);

        return encoded;
    }

    public static byte[] decode(Codec codec, byte[] encoded) {
        return Arrays.copyOfRange(encoded, codec.length(), encoded.length);
    }
}
