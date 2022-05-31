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
        //TODO check aloorithm
        return io.ipfs.multibase.Multibase.encode(io.ipfs.multibase.Multibase.Base.Base58BTC, data);
    }
}
