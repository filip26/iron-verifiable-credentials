package com.apicatalog.di;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.apicatalog.crypto.bc.BCECDSAVerifier;
import com.apicatalog.crypto.bc.BCEd25519Verifier;
import com.apicatalog.crypto.bc.BCMLDSAVerifier;
import com.apicatalog.crypto.bc.BCSLHDSAVerifier;
import com.apicatalog.di.suite.ECDSA2019;
import com.apicatalog.di.suite.EdDSA2022;
import com.apicatalog.di.suite.MLDSA2024;
import com.apicatalog.di.suite.SLHDSA2024;
import com.apicatalog.multibase.MultibaseDecoder;
import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.trust.MethodResolver;
import com.apicatalog.trust.ProofVerifier;
import com.apicatalog.trust.model.ContextAwareResolver;

public class VerifierTest {

    static ContextAwareResolver MODEL_RESOLVER = ContextAwareResolver.builder()
            // accept any context - for test purposes only
            .model(Predicate.not(Collection::isEmpty),
                    // in processing preferences order
                    Resources.SEMANTIC_MODEL,
                    Resources.LEXICAL_MODEL)
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

        var contexts = ContextAwareResolver.getContexts(signed);

        var model = MODEL_RESOLVER.resolve(contexts, signed);

        var processor = model.createAdapter(signed);
        
        var cursor = processor.createProofCursor();

        if (cursor == null || !cursor.next()) {
            fail("No proof(s) to verify");
            return;
        }

        do {

            if (!cursor.isAccepted()) {
                fail();
            }

            var proof = cursor.proof();
            var verified = PROOF_VERIFIER.verify(proof);

            assertTrue(verified);

        } while (cursor.next());

    }

    static final Stream<String> resources() {
        return Resources
                .stream()
                .filter(name -> name.endsWith(".signed.json"))
                .sorted();
    }
}
