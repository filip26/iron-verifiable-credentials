package com.apicatalog.di.suite;

import java.security.MessageDigest;
import java.security.SignatureException;
import java.util.function.Function;

import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.di.signature.ProofValue;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.trust.data.Data;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.signature.Signature;
import com.apicatalog.trust.signature.SignatureDecoder;

public class ECDSASuite {

    public static CryptoSuite newRDFC2019() {
        return new AtomicCryptoSuite(
                "ecdsa-rdfc-2019",
                "RDFC",
                Multibase.BASE_58_BTC,
                new Decoder(Multibase.BASE_58_BTC),
                ECDSASuite::generate);
    }

    public static CryptoSuite newJCS2019() {
        return new AtomicCryptoSuite(
                "ecdsa-jcs-2019",
                "JCS",
                Multibase.BASE_58_BTC,
                new Decoder(Multibase.BASE_58_BTC),
                ECDSASuite::generate);
    }

    private static class Decoder implements SignatureDecoder {

        private final Multibase multibase;

        public Decoder(Multibase multibase) {
            this.multibase = multibase;
        }

        @Override
        public Signature decode(String value, Proof proof, Data data) {

            var signature = multibase.decode(value);

            String algorithm = null;
            String digest = null;

            switch (signature.length) {
            case 64:
                algorithm = "P-256";
                digest = "SHA-256";
                break;
            case 96:
                algorithm = "P-384";
                digest = "SHA-384";
                break;
            default:
                throw new IllegalArgumentException();
            }

            return ProofValue.newSignature(
                    algorithm,
                    digest,
                    signature,
                    proof,
                    data);
        }
    }

    private static Signature generate(
            String algorithm,
            AsymmetricSigner signer,
            Function<String, MessageDigest> digestFactory,
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
