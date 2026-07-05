package com.apicatalog.di.suite;

import com.apicatalog.multibase.Multibase;

public final class CryptoSuites {

    public static AtomicCryptoSuite EDDSA_RDFC_2022 = new AtomicCryptoSuite(
            "eddsa-rdfc-2022",
            "Ed25519",
            "RDFC",
            "SHA-256",
            Multibase.BASE_58_BTC,
            64);

    public static AtomicCryptoSuite EDDSA_JCS_2022 = new AtomicCryptoSuite(
            "eddsa-jcs-2022",
            "Ed25519",
            "JCS",
            "SHA-256",
            Multibase.BASE_58_BTC,
            64);

    public static AtomicCryptoSuite ECDSA_RDFC_2019_P256 = new AtomicCryptoSuite(
            "ecdsa-rdfc-2019",
            "P-256",
            "RDFC",
            "SHA-256",
            Multibase.BASE_58_BTC,
            64);

    public static AtomicCryptoSuite ECDSA_RDFC_2019_P384 = new AtomicCryptoSuite(
            "ecdsa-rdfc-2019",
            "P-384",
            "RDFC",
            "SHA-384",
            Multibase.BASE_58_BTC,
            96);

    public static AtomicCryptoSuite ECDSA_JCS_2019_P256 = new AtomicCryptoSuite(
            "ecdsa-jcs-2019",
            "P-256",
            "JCS",
            "SHA-256",
            Multibase.BASE_58_BTC,
            64);

    public static AtomicCryptoSuite ECDSA_JCS_2019_P384 = new AtomicCryptoSuite(
            "ecdsa-jcs-2019",
            "P-384",
            "JCS",
            "SHA-384",
            Multibase.BASE_58_BTC,
            96);

    public static AtomicCryptoSuite getInstance(String id, String algorithm) {

        return switch (id) {
        case "eddsa-rdfc-2022" -> EDDSA_RDFC_2022;
        case "eddsa-jcs-2022" -> EDDSA_JCS_2022;

        case "ecdsa-rdfc-2019" ->
            switch (algorithm) {
            case "P-256" -> ECDSA_RDFC_2019_P256;
            case "P-384" -> ECDSA_RDFC_2019_P384;
            default -> throw new IllegalArgumentException();
            };

        case "ecdsa-jcs-2019" ->
            switch (algorithm) {
            case "P-256" -> ECDSA_JCS_2019_P256;
            case "P-384" -> ECDSA_JCS_2019_P384;
            default -> throw new IllegalArgumentException();
            };

        default -> throw new IllegalArgumentException();
        };
    }
}
