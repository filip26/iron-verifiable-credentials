package com.apicatalog.di;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.apicatalog.crypto.bc.BCECDSASigner;
import com.apicatalog.crypto.bc.BCEd25519Signer;
import com.apicatalog.crypto.bc.BCMLDSASigner;
import com.apicatalog.crypto.bc.BCSLHDSASigner;
import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.di.proof.Ed25519Signature2020;
import com.apicatalog.di.suite.ECDSA2019;
import com.apicatalog.di.suite.EdDSA2022;
import com.apicatalog.di.suite.MLDSA2024;
import com.apicatalog.di.suite.SLHDSA2024;
import com.apicatalog.di.suite.StandardCryptoSuite;
import com.apicatalog.jcs.Jcs;
import com.apicatalog.multibase.MultibaseDecoder;
import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.multicodec.codec.KeyCodec;
import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.trust.Document;
import com.apicatalog.trust.model.Model;
import com.apicatalog.trust.proof.Proof;

public class IssuerTest {

    static final MultibaseDecoder MULTIBASE = MultibaseDecoder.getInstance();

    static final MulticodecDecoder MULTICODEC = MulticodecDecoder.newInstance(
            KeyCodec.P256_PRIVATE,
            KeyCodec.P384_PRIVATE,
            KeyCodec.ED25519_PRIVATE,
            KeyCodec.MLDSA_44_PRIVATE,
            KeyCodec.SLHDSA_SHA2_128S_PRIVATE);

    @ParameterizedTest
    @MethodSource({ "resources" })
    void testIssue(String resource) throws Throwable {

        Map<String, String> keys = Resources.getMap(resource + ".keys.json");
        Map<String, Object> options = Resources.getMap(resource + ".options.json");
        Map<String, Object> document = Resources.getMap(resource + ".unsigned.json");

        var privateKey = MULTIBASE.decode(keys.get("secretKeyMultibase"));
        var privateKeyCodec = MULTICODEC.getCodec(privateKey).orElseThrow();

        final String signatureAlgorithm;
        final AsymmetricSigner signer;

        switch (privateKeyCodec.code()) {
        case KeyCodec.ED25519_PRIVATE_CODE:
            signatureAlgorithm = EdDSA2022.ALGORITHM;
            signer = BCEd25519Signer.newInstance(privateKeyCodec.decode(privateKey))::sign;
            break;
        // Use a secure random number generator to create non-deterministic signatures
        // for the algorithms below in production environments.
        case KeyCodec.P256_PRIVATE_CODE:
            signatureAlgorithm = ECDSA2019.P256;
            signer = BCECDSASigner.newP256Instance(privateKeyCodec.decode(privateKey))::sign;
            break;
        case KeyCodec.P384_PRIVATE_CODE:
            signatureAlgorithm = ECDSA2019.P384;
            signer = BCECDSASigner.newP384Instance(privateKeyCodec.decode(privateKey))::sign;
            break;
        case KeyCodec.MLDSA_44_PRIVATE_CODE:
            signatureAlgorithm = MLDSA2024.ALGORITHM_44;
            signer = BCMLDSASigner.new44Instance(privateKeyCodec.decode(privateKey))::sign;
            break;
        case KeyCodec.SLHDSA_SHA2_128S_PRIVATE_CODE:
            signatureAlgorithm = SLHDSA2024.ALGORITHM_SHA2_128s;
            signer = BCSLHDSASigner.new128sInstance(privateKeyCodec.decode(privateKey))::sign;
            break;

        default:
            throw new IllegalArgumentException(
                    """
                    Unsupported secret key algorithm %s (%d).
                    """
                            .formatted(privateKeyCodec.name(), privateKeyCodec.code()));
        }

        Proof proof = null;
        Map<String, ?> issued = null;

        if (DataIntegrityProof.TYPE_NAME.equals(options.get("type"))) {

            var cryptosuite = getCryptosuite((String) options.get("cryptosuite"));

            var proofDraft = cryptosuite.createProofDraft();
            proofDraft.options(options);

            var updater = getUpdater(cryptosuite.c14n()).apply(document);

            var payload = updater.createPayload();

            payload.withProofs(proofDraft.previous());

            proof = proofDraft.sign(
                    signatureAlgorithm,
                    signer,
                    Resources.DIGEST_FACTORY,
                    payload.digestible());

            updater.addProof(proofDraft.context(), DataIntegrityProof.compact((DataIntegrityProof) proof));
            issued = updater.compacted();

        } else if (Ed25519Signature2020.TYPE_NAME.equals(options.get("type"))) {

            assertEquals(Ed25519Signature2020.SIGNATURE_ALGORITHM, signatureAlgorithm);

            var proofDraft = Ed25519Signature2020.newInstance((Map<String, Object>) options);

            var updater = Resources.SEMANTIC_MODEL.createUpdater(document);

            var payload = updater.createPayload();

            proof = Ed25519Signature2020.generateProof(
                    signer,
                    Resources.DIGEST_FACTORY,
                    proofDraft,
                    payload.digestible());

//            Ed25519Signature2020.write(edProof, composer);

            // updater.addProof(proof);
            issued = updater.compacted();

        } else {
            fail("An unsupported proof type " + options.get("type"));
        }

        // verify the newly issued proof just for testing
        var verified = VerifierTest.PROOF_VERIFIER.verify(proof);
        assertTrue(verified);

        var expected = Resources.getMap(resource + ".signed.json");

        assertEquals(new String(Jcs.canonize(expected)), new String(Jcs.canonize(issued)));
    }

    static Function<Map<String, Object>, Document.Updater> getUpdater(String c14n) {
        return switch (c14n) {
        case Model.C14N_RDFC -> Resources.SEMANTIC_MODEL::createUpdater;
        case Model.C14N_JCS -> Resources.LEXICAL_MODEL::createUpdater;
        default -> throw new IllegalStateException(
                """
                Unsupported c14n = %s.
                """.formatted(c14n));
        };
    }

    static StandardCryptoSuite getCryptosuite(String id) {

        return switch (id) {
        case "eddsa-rdfc-2022" -> EdDSA2022.withRDFC();
        case "eddsa-jcs-2022" -> EdDSA2022.withJCS();

        case "ecdsa-rdfc-2019" -> ECDSA2019.withRDFC();
        case "ecdsa-jcs-2019" -> ECDSA2019.withJCS();

        case "mldsa44-rdfc-2024" -> MLDSA2024.get44withRDFC();
        case "mldsa44-jcs-2024" -> MLDSA2024.get44withJCS();

        case "slhdsa128-rdfc-2024" -> SLHDSA2024.get128withRDFC();
        case "slhdsa128-jcs-2024" -> SLHDSA2024.get128withJCS();

        default -> throw new IllegalArgumentException();
        };
    }

    static final Stream<String> resources() throws IOException {
        return Resources.stream()
                .filter(name -> name.endsWith("unsigned.json"))
                .map(name -> name.substring(0, name.indexOf('.')))
                .sorted();
    }
}
