package com.apicatalog.di;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.apicatalog.crypto.bc.BCECDSAVerifier;
import com.apicatalog.crypto.bc.BCEd25519Verifier;
import com.apicatalog.crypto.bc.BCMLDSAVerifier;
import com.apicatalog.crypto.bc.BCSLHDSAVerifier;
import com.apicatalog.di.barcodes.OpticalBarcode;
import com.apicatalog.di.suite.ECDSA2019;
import com.apicatalog.di.suite.EdDSA2022;
import com.apicatalog.di.suite.MLDSA2024;
import com.apicatalog.di.suite.SLHDSA2024;
import com.apicatalog.multibase.MultibaseDecoder;
import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.trust.MethodResolver;
import com.apicatalog.trust.ProofVerifier;
import com.apicatalog.trust.model.ModelResolver;
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
            .verifier(EdDSA2022.ALGORITHM, BCEd25519Verifier.getInstance()::verify)
            .verifier(ECDSA2019.P256, BCECDSAVerifier.getP256Instance()::verify)
            .verifier(ECDSA2019.P384, BCECDSAVerifier.getP384Instance()::verify)
            .verifier(MLDSA2024.ALGORITHM_44, BCMLDSAVerifier.get44Instance()::verify)
            .verifier(SLHDSA2024.ALGORITHM_SHA2_128s, BCSLHDSAVerifier.get128sInstance()::verify)
            .digestFactory(Resources.DIGEST_FACTORY)
            .build();

    @ParameterizedTest
    @MethodSource({ "resources" })
    void testVerify(String resource) throws Throwable {

        var signed = Resources.getMap(resource);

        var contexts = ModelResolver.getContexts(signed);

        var proofs = new ArrayList<Proof>();

        var model = Resources.SEMANTIC_MODEL_1;

        var cursor = model.createProofCursor(contexts, signed);

        if (cursor == null) {
            fail();
        }

        if (!cursor.next()) {
            fail("No proof(s) to verify");
        }

        int count = 0;

        do {
            count++;

            if (!cursor.isAccepted()) {
                continue;
            }

            var proof = cursor.proof();
            
            if (!(proof.signature().payload() instanceof OpticalBarcode barcode)) {
                fail();
                return;
            }
            
            byte[] XX = new byte[] { (byte) 188, 38, (byte) 200, (byte) 146, (byte) 227, (byte) 213, 90,
                    (byte) 250,
                    50, 18, 126, (byte) 254, 47, (byte) 177, 91, 23,
                    64, (byte) 129, 104, (byte) 223, (byte) 136, 81, 116, 67,
                    (byte) 136, 125, (byte) 137, (byte) 165, 117, 63, (byte) 152, (byte) 207 };

            
            barcode.opticalData(XX);
            
            var verified = PROOF_VERIFIER.verify(proof);

            assertTrue(verified);

            proofs.add(proof);

        } while (cursor.next());

        assertFalse(proofs.isEmpty());
    }

    static final Stream<String> resources() {
        return Resources
                .stream()
                .filter(name -> name.endsWith(".signed.json"))
                .sorted();
    }
}
