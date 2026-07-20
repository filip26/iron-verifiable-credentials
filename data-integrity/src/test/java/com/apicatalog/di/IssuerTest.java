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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.apicatalog.crypto.bc.BCECDSASigner;
import com.apicatalog.crypto.bc.BCEd25519Signer;
import com.apicatalog.crypto.bc.BCMLDSASigner;
import com.apicatalog.crypto.bc.BCSLHDSASigner;
import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.di.proof.Ed25519Signature2020;
import com.apicatalog.di.suite.StandardCryptoSuite;
import com.apicatalog.di.suite.ECDSA2019;
import com.apicatalog.di.suite.EdDSA2022;
import com.apicatalog.di.suite.MLDSA2024;
import com.apicatalog.di.suite.SLHDSA2024;
import com.apicatalog.jcs.Jcs;
import com.apicatalog.multibase.MultibaseDecoder;
import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.multicodec.codec.KeyCodec;
import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.tree.io.java.NativeComposer;
import com.apicatalog.trust.model.ContextAwareResolver;
import com.apicatalog.trust.model.ProcessingModel;
import com.apicatalog.trust.payload.PayloadGenerator;
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

        var proofs = document.get("proof");

        var context = ContextAwareResolver.getContexts(document);

        var composer = new NativeComposer<Map<String, ? extends Object>>();

        if (DataIntegrityProof.TYPE_NAME.equals(options.get("type"))) {

            var cryptosuite = getCryptosuite((String) options.get("cryptosuite"));

            var proofDraft = cryptosuite.createProofDraft();
            proofDraft.options(options);

            var payload = getPayload(cryptosuite.c14n()).apply(document);
            
            payload.withProofs(proofDraft.previous());

            var integrityProof = proofDraft.sign(
                    signatureAlgorithm,
                    signer,
                    Resources.DIGEST_FACTORY,
                    payload.digestible());

//TODO
//            processor.add(proof);
//
//            processor.write(composer);

            DataIntegrityProof.write(integrityProof, composer);

            if (proofDraft.context() != null && !proofDraft.context().isEmpty()) {
                document.put("@context", merge(context, proofDraft.context()));
            }

            proof = integrityProof;

        } else if (Ed25519Signature2020.TYPE_NAME.equals(options.get("type"))) {

            assertEquals(Ed25519Signature2020.SIGNATURE_ALGORITHM, signatureAlgorithm);

            var proofDraft = Ed25519Signature2020.newInstance((Map<String, Object>) options);

            var payload = Resources.SEMANTIC_MODEL.createPayload(document);
            
            var edProof = Ed25519Signature2020.generateProof(
                    signer,
                    Resources.DIGEST_FACTORY,
                    proofDraft,
                    payload.digestible());

            Ed25519Signature2020.write(edProof, composer);

            if (proofDraft.context() != null && !proofDraft.context().isEmpty()) {
                document.put("@context", merge(context, proofDraft.context()));
            }

            proof = edProof;

        } else {
            fail("An unsupported proof type " + options.get("type"));
        }

        // verify the newly issued proof just for testing
        var verified = VerifierTest.PROOF_VERIFIER.verify(proof);
        assertTrue(verified);

        // TODO remove, hide in document handler interface
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

    static Function<Map<String, Object>, PayloadGenerator> getPayload(String c14n) {
        return switch (c14n) {
        case ProcessingModel.C14N_RDFC -> Resources.SEMANTIC_MODEL::createPayload;
        case ProcessingModel.C14N_JCS -> Resources.LEXICAL_MODEL::createPayload;
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
        var result = LinkedHashSet.<String>newLinkedHashSet(documentContext.size() + proofContext.size());
        result.addAll(documentContext);
        result.addAll(proofContext);
        return result;
    }
}
