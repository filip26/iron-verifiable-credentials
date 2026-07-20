package com.apicatalog.di;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.apicatalog.crypto.bc.BCECDSAVerifier;
import com.apicatalog.di.barcodes.ECDSAXI2023.BarcodePayload;
import com.apicatalog.di.suite.ECDSA2019;
import com.apicatalog.multibase.MultibaseDecoder;
import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.trust.MethodResolver;
import com.apicatalog.trust.ProofVerifier;
import com.apicatalog.trust.model.ContextAwareResolver;
import com.apicatalog.trust.proof.Proof;

public class VerifierTest {

    static MethodResolver DID_KEY_RESOLVER = proof -> {
        if (!proof.verificationMethod().startsWith("did:key:")) {
            throw new IllegalArgumentException();
        }

        String based = null;
        var fragmentIndex = proof.verificationMethod().indexOf('#');
        if (fragmentIndex != -1) {
            based = proof.verificationMethod().substring("did:key:".length(), fragmentIndex);
        } else {
            based = proof.verificationMethod().substring("did:key:".length());
        }

        var key = MultibaseDecoder.getInstance().decode(based);

        var codec = MulticodecDecoder.newInstance().getCodec(key).orElseThrow();

        // TODO check the key codec vs proof.signature().algorithm()

        return codec.decode(key);

    };

    static ProofVerifier PROOF_VERIFIER = ProofVerifier.newBuilder()
            // TODO allow list concrete DI cryptosuites only OR list models and
            // configurations
            .resolver(DID_KEY_RESOLVER)
            .verifier(ECDSA2019.P256, BCECDSAVerifier.getP256Instance()::verify)
            .verifier(ECDSA2019.P384, BCECDSAVerifier.getP384Instance()::verify)
            .digestFactory(Resources.DIGEST_FACTORY)
            .build();

    @ParameterizedTest
    @MethodSource({ "resources" })
    void testVerify(String resource) throws Throwable {

        Map<String, Object> options = Resources.getMap(resource + ".options.json");
        Map<String, Object> signed = Resources.getMap(resource + ".signed.json");

        var contexts = ContextAwareResolver.getContexts(signed);

        var proofs = new ArrayList<Proof>();

        var processor = Resources.SEMANTIC_MODEL.createAdapter(signed);
        
        var cursor = processor.createProofCursor();

        if (cursor == null || !cursor.next()) {
            fail("No proof(s) to verify");
        }

        do {
            if (!cursor.isAccepted()) {
                continue;
            }

            var proof = cursor.proof();

            if (!(proof.signature().payload() instanceof BarcodePayload barcode)) {
                fail();
                return;
            }

            barcode.opticalData(((Collection<?>) options.get("opticalDataBytes"))
                    .stream().map(BigInteger.class::cast).map(BigInteger::byteValue)
                    .collect(ByteArrayOutputStream::new, ByteArrayOutputStream::write, (_, _) -> {
                    })
                    .toByteArray());

            var verified = PROOF_VERIFIER.verify(proof);

            assertTrue(verified);

            proofs.add(proof);

        } while (cursor.next());
    }

    static final Stream<String> resources() {
        return Resources
                .stream()
                .filter(name -> name.endsWith(".signed.json"))
                .map(name -> name.substring(0, name.indexOf('.')))
                .sorted();
    }
}
