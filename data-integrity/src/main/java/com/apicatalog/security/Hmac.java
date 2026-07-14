package com.apicatalog.security;

@FunctionalInterface
public interface Hmac {

    public static final String HMAC_SHA_256 = "HMAC-SHA-256";
    public static final String HMAC_SHA_512 = "HMAC-SHA-512";
    public static final String HMAC_SHA_3 = "HMAC-SHA-3";

    @FunctionalInterface
    interface Factory {
        Hmac newHmac(String algorithm, byte[] hmacKey);
    }

    byte[] compute(byte[] data);
}
