package com.apicatalog.multibase;

public class Multibase {

    public enum Algorithm {
        Base58Btc,
    }

    public static boolean isAlgorithmSupported(String encoded) {
        return encoded != null && !encoded.isEmpty() && 'z' == encoded.toCharArray()[0];
    }

    public static byte[] decode(String encoded) {
        return io.ipfs.multibase.Multibase.decode(encoded);
    }

    public static String encode(Algorithm algorithm, byte[] data) {

        if (algorithm == null) {
            throw new IllegalArgumentException("The 'algorithm' parameter must not be null.");
        }
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("The 'data' parameter must not be an empty array nor null.");
        }

        switch (algorithm) {
        case Base58Btc:
            return io.ipfs.multibase.Multibase.encode(io.ipfs.multibase.Multibase.Base.Base58BTC, data);

        default:
            break;
        }

        throw new IllegalArgumentException("Unsupported algorithm [" + algorithm + "].");
    }
}
