package com.apicatalog.di.suite;

import java.security.MessageDigest;
import java.util.function.Function;

import com.apicatalog.di.signature.ProofValueDecoder;
import com.apicatalog.di.signature.ProofValueGenerator;
import com.apicatalog.multibase.Multibase;

public class MLDSA44Suite {

    public static CryptoSuite newRDFC2024(Function<String, MessageDigest> digestFactory) {

        var digestor = digestFactory.apply("SHA-256");

        return new AtomicCryptoSuite(
                "mldsa44-rdfc-2024",
                "RDFC",
                Multibase.BASE_64_URL,
                new ProofValueDecoder("ML-DSA-44", Multibase.BASE_64_URL, digestor, 2420),
                new ProofValueGenerator(digestor),
                2420);
    }

    public static CryptoSuite newJCS2024(Function<String, MessageDigest> digestFactory) {

        var digestor = digestFactory.apply("SHA-256");

        return new AtomicCryptoSuite(
                "mldsa44-jcs-2024",
                "JCS",
                Multibase.BASE_64_URL,
                new ProofValueDecoder("ML-DSA-44", Multibase.BASE_64_URL, digestor, 2420),
                new ProofValueGenerator(digestor),
                2420);
    }
}
