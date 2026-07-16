package com.apicatalog.di;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import com.apicatalog.di.barcodes.ECDSAXI2023;
import com.apicatalog.di.barcodes.ECDSAXI2023.BarcodePayload;
import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.di.suite.ECDSA2019;
import com.apicatalog.jcs.Jcs;
import com.apicatalog.multibase.MultibaseDecoder;
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
            KeyCodec.P384_PRIVATE);

    @ParameterizedTest
    @MethodSource({ "resources" })
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

        var cryptosuite = ECDSAXI2023.getInstance();

        var proofDraft = cryptosuite.createProofDraft();
        proofDraft.options(options);

        var processor = getProcessor(proofDraft.c14n()).apply(document);

        processor.withProofs(proofDraft.previous());

        var payload = processor.digestible(BarcodePayload::new);

        byte[] XX = new byte[] { (byte) 188, 38, (byte) 200, (byte) 146, (byte) 227, (byte) 213, 90,
                (byte) 250,
                50, 18, 126, (byte) 254, 47, (byte) 177, 91, 23,
                64, (byte) 129, 104, (byte) 223, (byte) 136, 81, 116, 67,
                (byte) 136, 125, (byte) 137, (byte) 165, 117, 63, (byte) 152, (byte) 207 };

        payload.opticalData(XX);

        proof = proofDraft.sign(
                keyAlgorithm,
                signer,
                Resources.DIGEST_FACTORY,
                payload);

        DataIntegrityProof.write((DataIntegrityProof) proof, composer);

        if (proofDraft.context() != null && !proofDraft.context().isEmpty()) {
            document.put("@context", merge((Collection) document.get("@context"), proofDraft.context()));
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
