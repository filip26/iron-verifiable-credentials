package com.apicatalog.di.suite;

import java.security.MessageDigest;
import java.util.function.Function;

import com.apicatalog.di.signature.ProofValueDecoder;
import com.apicatalog.di.signature.ProofValueGenerator;
import com.apicatalog.multibase.Multibase;

public class EdDSASuite {

    public static CryptoSuite newRDFC2022(Function<String, MessageDigest> digestFactory) {

        var digestor = digestFactory.apply("SHA-256");

        return new AtomicCryptoSuite(
                "eddsa-rdfc-2022",
                "RDFC",
                Multibase.BASE_58_BTC,
                new ProofValueDecoder("Ed25519", Multibase.BASE_58_BTC, digestor, 64),
                new ProofValueGenerator(digestor),
                64);
    }

    public static CryptoSuite newJCS2022(Function<String, MessageDigest> digestFactory) {

        var digestor = digestFactory.apply("SHA-256");

        return new AtomicCryptoSuite(
                "eddsa-jcs-2022",
                "JCS",
                Multibase.BASE_58_BTC,
                new ProofValueDecoder("Ed25519", Multibase.BASE_58_BTC, digestor, 64),
                new ProofValueGenerator(digestor),
                64);
    }
}
