package com.apicatalog.security;

@FunctionalInterface
public interface Digestor {

    public static final String SHA_256 = "SHA-256";
    public static final String SHA_384 = "SHA-384";
    public static final String SHA_512 = "SHA-512";

    @FunctionalInterface
    interface Factory {
        Digestor newDigestor(String algorithm);
    }

    byte[] digest(byte[] data);

}
