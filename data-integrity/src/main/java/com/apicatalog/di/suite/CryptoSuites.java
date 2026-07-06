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

    public static AtomicCryptoSuite MLDSA44_RDFC_2024 = new AtomicCryptoSuite(
            "mldsa44-rdfc-2024",
            "ML-DSA-44",
            "RDFC",
            "SHA-256",
            Multibase.BASE_64_URL,
            2420);

    public static AtomicCryptoSuite MLDSA44_JCS_2024 = new AtomicCryptoSuite(
            "mldsa44-jcs-2024",
            "ML-DSA-44",
            "JCS",
            "SHA-256",
            Multibase.BASE_64_URL,
            2420);

    public static AtomicCryptoSuite SLHDSA128_RDFC_2024 = new AtomicCryptoSuite(
            "slhdsa128-rdfc-2024",
            "SLH-DSA-SHA2-128s",
            "RDFC",
            "SHA-256",
            Multibase.BASE_64_URL,
            7856);

    public static AtomicCryptoSuite SLHDSA128_JCS_2024 = new AtomicCryptoSuite(
            "slhdsa128-jcs-2024",
            "SLH-DSA-SHA2-128s",
            "JCS",
            "SHA-256",
            Multibase.BASE_64_URL,
            7856);

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

        case "mldsa44-rdfc-2024" -> MLDSA44_RDFC_2024;
        case "mldsa44-jcs-2024" -> MLDSA44_JCS_2024;

        case "slhdsa128-rdfc-2024" -> SLHDSA128_RDFC_2024;
        case "slhdsa128-jcs-2024" -> SLHDSA128_JCS_2024;

        default -> throw new IllegalArgumentException();
        };
    }
}
