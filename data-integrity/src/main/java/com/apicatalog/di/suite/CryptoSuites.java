package com.apicatalog.di.suite;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.function.Function;

import com.apicatalog.di.signature.ProofValueDecoder;
import com.apicatalog.di.signature.ProofValueGenerator;
import com.apicatalog.multibase.Multibase;

@Deprecated
public final class CryptoSuites {

    // BTC58: 64 = 64-88, 96 = 96-132, 32 = 32–44

    public static void main(String[] args) {

        byte[] x = new byte[32];
        Arrays.fill(x, (byte) 0);
        IO.println(Multibase.BASE_58_BTC.encode(x).length());
        Arrays.fill(x, (byte) 0xff);
        IO.println(Multibase.BASE_58_BTC.encode(x).length());
    }

//    public static final String EDDSA_RDFC_2022 = "eddsa-rdfc-2022";
//    public static final String EDDSA_JCS_2022 = "eddsa-jcs-2022";
//
//    public static final String ECDSA_RDFC_2019_P256 = "ecdsa-rdfc-2019";
//    public static final String ECDSA_JCS_2019_P256 = null;
//    
//    public static final String ECDSA_RDFC_2019_P384 = "ecdsa-rdfc-2019";
//    public static final String ECDSA_JCS_2019_P384 = null;
//    
//    public static final String MLDSA44_RDFC_2024 = null;
//    public static final String MLDSA44_JCS_2024 = null;
//    
//    public static final String SLHDSA128_RDFC_2024 = null;
//    public static final String SLHDSA128_JCS_2024 = null;



//    public static AtomicCryptoSuite newEDDSAJCS2022(Function<String, MessageDigest> digestFactory) {
//        return new AtomicCryptoSuite(
//                "eddsa-jcs-2022",
//                "Ed25519",
//                "JCS",
//                "SHA-256",
//                Multibase.BASE_58_BTC,
//                new ProofValueDecoder("Ed25519", Multibase.BASE_58_BTC, digestFactory.apply("SHA-256"), 64),
//                64);
//    }
//
//    public static AtomicCryptoSuite newECDSARDFC2019P256(Function<String, MessageDigest> digestFactory) {
//        return new AtomicCryptoSuite(
//                "ecdsa-rdfc-2019",
//                "P-256",
//                "RDFC",
//                "SHA-256",
//                Multibase.BASE_58_BTC,
//                new ProofValueDecoder("P-256", Multibase.BASE_58_BTC, digestFactory.apply("SHA-256"), 64),
//                64);
//    }
//
//    public static AtomicCryptoSuite newECDSARDFC2019P384(Function<String, MessageDigest> digestFactory) {
//        return new AtomicCryptoSuite(
//                "ecdsa-rdfc-2019",
//                "P-384",
//                "RDFC",
//                "SHA-384",
//                Multibase.BASE_58_BTC,
//                new ProofValueDecoder("P-384", Multibase.BASE_58_BTC, digestFactory.apply("SHA-384"), 96),
//                96);
//    }
//
//    public static AtomicCryptoSuite newECDSAJCS2019P256(Function<String, MessageDigest> digestFactory) {
//        return new AtomicCryptoSuite(
//                "ecdsa-jcs-2019",
//                "P-256",
//                "JCS",
//                "SHA-256",
//                Multibase.BASE_58_BTC,
//                new ProofValueDecoder("P-256", Multibase.BASE_58_BTC, digestFactory.apply("SHA-256"), 64),
//                64);
//    }
//
//    public static AtomicCryptoSuite newECDSAJCS2019P384(Function<String, MessageDigest> digestFactory) {
//        return new AtomicCryptoSuite(
//                "ecdsa-jcs-2019",
//                "P-384",
//                "JCS",
//                "SHA-384",
//                Multibase.BASE_58_BTC,
//                new ProofValueDecoder("P-384", Multibase.BASE_58_BTC, digestFactory.apply("SHA-384"), 96),
//                96);
//    }
//
//    public static AtomicCryptoSuite newMLDSA44RDFC2024(Function<String, MessageDigest> digestFactory) {
//        return new AtomicCryptoSuite(
//                "mldsa44-rdfc-2024",
//                "ML-DSA-44",
//                "RDFC",
//                "SHA-256",
//                Multibase.BASE_64_URL,
//                new ProofValueDecoder("ML-DSA-44", Multibase.BASE_64_URL, digestFactory.apply("SHA-256"), 2420),
//                2420);
//
//    }
//
//    public static AtomicCryptoSuite newMLDSA44JCS2024(Function<String, MessageDigest> digestFactory) {
//        return new AtomicCryptoSuite(
//                "mldsa44-jcs-2024",
//                "ML-DSA-44",
//                "JCS",
//                "SHA-256",
//                Multibase.BASE_64_URL,
//                new ProofValueDecoder("ML-DSA-44", Multibase.BASE_64_URL, digestFactory.apply("SHA-256"), 2420),
//                2420);
//    }
//
//    public static CryptoSuite newSLHDSA128RDFC2024(Function<String, MessageDigest> digestFactory) {
//        return new AtomicCryptoSuite(
//                "slhdsa128-rdfc-2024",
//                "SLH-DSA-SHA2-128s",
//                "RDFC",
//                "SHA-256",
//                Multibase.BASE_64_URL,
//                new ProofValueDecoder("SLH-DSA-SHA2-128s", Multibase.BASE_64_URL, digestFactory.apply("SHA-256"), 7856),
//                7856);
//    }
//
//    public static CryptoSuite newSLHDSA128JCS2024(Function<String, MessageDigest> digestFactory) {
//        return new AtomicCryptoSuite(
//                "slhdsa128-jcs-2024",
//                "SLH-DSA-SHA2-128s",
//                "JCS",
//                "SHA-256",
//                Multibase.BASE_64_URL,
//                new ProofValueDecoder("SLH-DSA-SHA2-128s", Multibase.BASE_64_URL, digestFactory.apply("SHA-256"), 7856),
//                7856);
//    }

}
