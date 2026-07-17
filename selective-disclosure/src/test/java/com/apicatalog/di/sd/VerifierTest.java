package com.apicatalog.di.sd;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.apicatalog.crypto.bc.BCECDSAVerifier;
import com.apicatalog.di.suite.ECDSA2019;
import com.apicatalog.multibase.MultibaseDecoder;
import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.trust.MethodResolver;
import com.apicatalog.trust.ProofVerifier;
import com.apicatalog.trust.model.ModelResolver;
import com.apicatalog.trust.proof.Proof;

public class VerifierTest {

    static ModelResolver MODEL_RESOLVER = ModelResolver.newBuilder()
            // accept any context - for test purposes only
            .model(Predicate.not(Collection::isEmpty), Resources.MODEL)
            .build();

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

        var signed = Resources.getMap(resource);

        var contexts = ModelResolver.getContexts(signed);

        var models = MODEL_RESOLVER.resolve(contexts, signed);

        assertFalse(models.isEmpty());

        var proofs = new ArrayList<Proof>();

        int lastCount = -1;

        for (var model : models) {

            var cursor = model.createProofCursor(contexts, signed);

            if (cursor == null) {
                continue;
            }

            if (!cursor.next()) {
                fail("No proof(s) to verify");
                return;
            }

            int count = 0;

            do {
                count++;

                if (!cursor.isAccepted()) {
                    continue;
                }

                var proof = cursor.proof();

                var verified = PROOF_VERIFIER.verify(proof);

                assertTrue(verified);

                proofs.add(proof);

            } while (cursor.next());

            if (lastCount != -1 && lastCount != count) {
                throw new IllegalArgumentException("Inconsistent proofs size");
            }
            lastCount = count;

            // no unknown proofs, all proofs have been processed, terminate
            if (lastCount == proofs.size()) {
                break;
            }

        }
        assertFalse(proofs.isEmpty());
    }

    static final Stream<String> resources() {
        return Resources
                .stream()
                .filter(name -> name.endsWith(".signed.json") || name.endsWith(".derived.json"))
                .sorted();
    }
}
