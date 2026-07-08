package com.apicatalog.di.suite;

import com.apicatalog.di.signature.ProofValueDecoder;
import com.apicatalog.di.signature.ProofValueGenerator;
import com.apicatalog.multibase.Multibase;

public class EdDSASuite {

    public static CryptoSuite newRDFC2022() {

        return new AtomicCryptoSuite(
                "eddsa-rdfc-2022",
                "RDFC",
                Multibase.BASE_58_BTC,
                new ProofValueDecoder("Ed25519", "SHA-256", Multibase.BASE_58_BTC, 64),
                new ProofValueGenerator("SHA-256"));
    }

    public static CryptoSuite newJCS2022() {
        return new AtomicCryptoSuite(
                "eddsa-jcs-2022",
                "JCS",
                Multibase.BASE_58_BTC,
                new ProofValueDecoder("Ed25519", "SHA-256", Multibase.BASE_58_BTC, 64),
                new ProofValueGenerator("SHA-256"));
    }
}
