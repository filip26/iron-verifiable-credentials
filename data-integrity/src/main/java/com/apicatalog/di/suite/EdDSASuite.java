package com.apicatalog.di.suite;

import com.apicatalog.di.signature.ProofValue;
import com.apicatalog.di.signature.ProofValueGenerator;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.trust.data.Data;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.signature.Signature;

public class EdDSASuite {

    public static final int SIGNATURE_LENGTH = 64;
    
    public static CryptoSuite newRDFC2022() {

        return new AtomicCryptoSuite(
                "eddsa-rdfc-2022",
                "RDFC",
                Multibase.BASE_58_BTC,
                EdDSASuite::decode,
                ProofValueGenerator::generateWithSHA256);
    }

    public static CryptoSuite newJCS2022() {
        return new AtomicCryptoSuite(
                "eddsa-jcs-2022",
                "JCS",
                Multibase.BASE_58_BTC,
                EdDSASuite::decode,
                ProofValueGenerator::generateWithSHA256);
    }
    
    static Signature decode(String value, Proof proof, Data data) {

        var signature = Multibase.BASE_58_BTC.decode(value);

        if (signature.length != SIGNATURE_LENGTH) {
            throw new IllegalArgumentException(
                    """
                    ... invalid signature size ... %d bytes, expected %d bytes.
                    """.formatted(signature.length, SIGNATURE_LENGTH));
        }

        return ProofValue.newSignature(
                "Ed25519",
                "SHA-256",
                signature,
                proof,
                data);
    }

}
