package com.apicatalog.di;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.apicatalog.crypto.bc.BCECDSASigner;
import com.apicatalog.crypto.bc.BCEd25519Signer;
import com.apicatalog.crypto.bc.BCMLDSASigner;
import com.apicatalog.crypto.bc.BCSLHDSASigner;
import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.di.proof.Ed25519Signature2020;
import com.apicatalog.di.std.StandardCryptoSuite;
import com.apicatalog.di.suite.ECDSA2019;
import com.apicatalog.di.suite.EdDSA2022;
import com.apicatalog.di.suite.MLDSA2024;
import com.apicatalog.di.suite.SLHDSA2024;
import com.apicatalog.jcs.Jcs;
import com.apicatalog.multibase.MultibaseDecoder;
import com.apicatalog.multicodec.Multicodec;
import com.apicatalog.multicodec.Multicodec.Tag;
import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.multicodec.codec.KeyCodec;
import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.tree.io.java.NativeComposer;
import com.apicatalog.trust.model.DataModel;
import com.apicatalog.trust.processor.PayloadProcessor;
import com.apicatalog.trust.proof.Proof;

public class IssuerTest {

    static final MultibaseDecoder MULTIBASE = MultibaseDecoder.getInstance();

    static final MulticodecDecoder MULTICODEC = MulticodecDecoder.newInstance(
            KeyCodec.P256_PRIVATE,
            KeyCodec.P384_PRIVATE,
            KeyCodec.ED25519_PRIVATE,
            KeyCodec.MLDSA_44_PRIVATE,
            KeyCodec.SLHDSA_SHA2_128S_PRIVATE,
            // TODO remove when multicodec is updated
            Multicodec.of("falcon-512-pub", Tag.Key, 4652));

    @ParameterizedTest
    @MethodSource({ "resources" })
    @Disabled
    void testIssue(String resource) throws Throwable {

        Map<String, String> keys = Resources.getMap(resource + ".keys.json");
        Map<String, Object> options = Resources.getMap(resource + ".options.json");
        Map<String, Object> document = Resources.getMap(resource + ".unsigned.json");

        var privateKey = MULTIBASE.decode(keys.get("secretKeyMultibase"));
        var privateKeyCodec = MULTICODEC.getCodec(privateKey).orElseThrow();

        final String keyAlgorithm;
        final AsymmetricSigner signer;

        switch (privateKeyCodec.code()) {
        // Use a secure random number generator to create non-deterministic signatures
        // for the algorithms below in production environments.
        case KeyCodec.P256_PRIVATE_CODE:
            keyAlgorithm = ECDSA2019.P256;
            signer = BCECDSASigner.newP256Instance(privateKeyCodec.decode(privateKey))::sign;
            break;
        case KeyCodec.P384_PRIVATE_CODE:
            keyAlgorithm = ECDSA2019.P384;
            signer = BCECDSASigner.newP384Instance(privateKeyCodec.decode(privateKey))::sign;
            break;
        default:
            throw new IllegalArgumentException(
                    """
                    Unsupported secret key algorithm %s (%d).
                    """
                            .formatted(privateKeyCodec.name(), privateKeyCodec.code()));
        }

        Proof proof = null;

        var proofs = document.get("proof");

        var composer = new NativeComposer<Map<String, ? extends Object>>();

        if (DataIntegrityProof.TYPE_NAME.equals(options.get("type"))) {

            var cryptosuite = getCryptosuite((String) options.get("cryptosuite"));

            var proofDraft = cryptosuite.createProofDraft();
            proofDraft.options(options);

            var processor = getProcessor(proofDraft.c14n()).apply(document);

            processor.withProofs(proofDraft.previous());

            proof = proofDraft.sign(
                    keyAlgorithm,
                    signer,
                    Resources.DIGEST_FACTORY,
                    processor.digestible());

            DataIntegrityProof.write((DataIntegrityProof) proof, composer);

            if (proofDraft.context() != null && !proofDraft.context().isEmpty()) {
                document.put("@context", merge((Collection) document.get("@context"), proofDraft.context()));
            }

        } else if (Ed25519Signature2020.TYPE_NAME.equals(options.get("type"))) {

            assertEquals(Ed25519Signature2020.SIGNATURE_ALGORITHM, keyAlgorithm);

            var proofDraft = Ed25519Signature2020.newDraft((Map) options);

            var processor = Resources.SEMANTIC_MODEL_1.createProcessor(document);

            proof = Ed25519Signature2020.generateProof(
                    signer,
                    Resources.DIGEST_FACTORY,
                    proofDraft,
                    processor.digestible());

            Ed25519Signature2020.write((Ed25519Signature2020) proof, composer);

            if (proofDraft.context() != null && !proofDraft.context().isEmpty()) {
                document.put("@context", merge((Collection) document.get("@context"), proofDraft.context()));
            }

        } else {
            fail("An unsupported proof type " + options.get("type"));
        }

        var verified = VerifierTest.PROOF_VERIFIER.verify(proof);
        assertTrue(verified);

        var proofMap = composer.compose();

        if (proofs instanceof Collection col) {
            var clone = new ArrayList<>(col);
            col.add(proofMap);
            proofs = col;

        } else if (proofs == null) {
            proofs = proofMap;

        } else {
            var col = new ArrayList<>();
            col.add(proofs);
            col.add(proofMap);
            proofs = col;
        }

        document.put("proof", proofs);

        var expected = Resources.getMap(resource + ".signed.json");

        assertEquals(new String(Jcs.canonize(expected)), new String(Jcs.canonize(document)));
    }

    static Function<Map<String, Object>, PayloadProcessor> getProcessor(String c14n) {
        return switch (c14n) {
        case DataModel.C14N_RDFC -> Resources.SEMANTIC_MODEL_1::createProcessor;
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

    static Collection<String> merge(Collection<String> documentContext, Collection<String> proofContext) {

        var result = new LinkedHashSet<>(documentContext);

        result.addAll(proofContext);

        return result;
    }
}
