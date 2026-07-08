package com.apicatalog.di.suite;

import java.security.MessageDigest;
import java.security.SignatureException;
import java.util.function.Function;

import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.di.signature.ProofValue;
import com.apicatalog.di.signature.ProofValueDecoder;
import com.apicatalog.di.signature.ProofValueGenerator;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.trust.data.Data;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.signature.Signature;
import com.apicatalog.trust.signature.SignatureDecoder;
import com.apicatalog.trust.signature.SignatureGenerator;

public class ECDSASuite {

    public static CryptoSuite newRDFC2019(Function<String, MessageDigest> digestFactory) {

        var digestor = digestFactory.apply("SHA-256");

        return new AtomicCryptoSuite(
                "ecdsa-rdfc-2019",
                "RDFC",
                Multibase.BASE_58_BTC,
                new Decoder(Multibase.BASE_58_BTC, digestFactory),
                new Generator(digestFactory),
                64);
    }

    public static CryptoSuite newJCS2019(Function<String, MessageDigest> digestFactory) {
        return new AtomicCryptoSuite(
                "ecdsa-jcs-2019",
                "JCS",
                Multibase.BASE_58_BTC,
                new Decoder(Multibase.BASE_58_BTC, digestFactory),
                new Generator(digestFactory),
                96);
    }

    private static class Decoder implements SignatureDecoder {

        private final Multibase multibase;
        private final Function<String, MessageDigest> digestFactory;

        public Decoder(Multibase multibase, Function<String, MessageDigest> digestFactory) {
            this.multibase = multibase;
            this.digestFactory = digestFactory;
        }

        @Override
        public Signature decode(String value, Proof proof, Data data) {

            String algorithm = null;
            MessageDigest digest = null;

            // BTC58: 64 = 64-88, 96 = 96-132
            if (value.length() >= 65 && value.length() <= 89) {
                algorithm = "P-256";
                digest = digestFactory.apply("SHA-256");

            } else if (value.length() >= 97 && value.length() <= 133) {
                algorithm = "P-384";
                digest = digestFactory.apply("SHA-384");

            } else {
                throw new IllegalArgumentException();
            }

            return ProofValue.newSignature(
                    algorithm,
                    digest,
                    multibase.decode(value),
                    proof,
                    data);
        }

    }

    private static class Generator implements SignatureGenerator<DataIntegrityProof> {

        private final Function<String, MessageDigest> digestFactory;

        public Generator(Function<String, MessageDigest> digestFactory) {
            this.digestFactory = digestFactory;
        }

        @Override
        public Signature generate(
                String algorithm,
                AsymmetricSigner signer,
                DataIntegrityProof proof,
                Data data)
                throws SignatureException {

            var digestor = switch (algorithm) {
            case "P-256" -> digestFactory.apply("SHA-256");
            case "P-384" -> digestFactory.apply("SHA-384");
            default -> throw new IllegalArgumentException();
            };

            return ProofValue.generateSignature(
                    algorithm,
                    signer,
                    digestor,
                    proof,
                    data);
        }

    }

}
