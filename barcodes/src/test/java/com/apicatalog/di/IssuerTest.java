package com.apicatalog.di;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
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

        DataIntegrityProof proof = null;

        var cryptosuite = ECDSAXI2023.getInstance();

        var proofDraft = cryptosuite.createProofDraft();
        proofDraft.options(options);

        var updater = Resources.SEMANTIC_MODEL.createUpdater(document);

        var payloadProvider = updater.createPayload();

        payloadProvider.withProofs(proofDraft.previous());

        var payload = payloadProvider.digestible(BarcodePayload::new);

        payload.opticalData(((Collection<?>) options.get("opticalDataBytes"))
                .stream().map(BigInteger.class::cast).map(BigInteger::byteValue)
                .collect(ByteArrayOutputStream::new, ByteArrayOutputStream::write, (_, _) -> {
                })
                .toByteArray());

        proof = proofDraft.sign(
                keyAlgorithm,
                signer,
                Resources.DIGEST_FACTORY,
                payload);

        updater.addProof(
                proof.context(),
                DataIntegrityProof.compact(proof));

        var issued = updater.compacted();

        var verified = VerifierTest.PROOF_VERIFIER.verify(proof);
        assertTrue(verified);

        var expected = Resources.getMap(resource + ".signed.json");
        assertEquals(new String(Jcs.canonize(expected)), new String(Jcs.canonize(issued)));
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
