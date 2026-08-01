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
import com.apicatalog.did.key.DidKey;
import com.apicatalog.did.key.DidKeyResolver;
import com.apicatalog.did.resolver.MultiKeyResolver;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multibase.MultibaseDecoder;
import com.apicatalog.multicodec.codec.KeyCodec;
import com.apicatalog.trust.model.ContextAwareResolver;
import com.apicatalog.trust.proof.ProofVerifier;

public class VerifierTest {

    static final ContextAwareResolver MODEL_RESOLVER = ContextAwareResolver.newBuilder()
            // accept any context - for test purposes only
            .model(Predicate.not(Collection::isEmpty),
                    // in processing preferences order
                    Resources.SEMANTIC_MODEL,
                    Resources.LEXICAL_MODEL)
            .build();

    static final DidKeyResolver DID_KEY_RESOLVER = DidKeyResolver.newBuilder()
            .multibaseDecoder(MultibaseDecoder.getInstance(
                    Multibase.BASE_58_BTC,
                    Multibase.BASE_64_URL)::decode)
            .multikey()
            .build();

    static final MultiKeyResolver MULTIKEY_RESOLVER = MultiKeyResolver.newBuilder()
            .codec(EdDSA2022.ALGORITHM, KeyCodec.ED25519_PUBLIC.varint())
            .codec(ECDSA2019.P256, KeyCodec.P256_PUBLIC.varint())
            .codec(ECDSA2019.P384, KeyCodec.P384_PUBLIC.varint())
            .codec(MLDSA2024.ALGORITHM_44, KeyCodec.MLDSA_44_PUBLIC.varint())
            .codec(SLHDSA2024.ALGORITHM_SHA2_128s, KeyCodec.SLHDSA_SHA2_128S_PUBLIC.varint())
            .methodResolver(DidKey.METHOD_NAME, DID_KEY_RESOLVER)
            .documentResolver(DidKey.METHOD_NAME, DID_KEY_RESOLVER)
            .build();

    static ProofVerifier PROOF_VERIFIER = ProofVerifier.newBuilder()
            .publicKeyResolver(MULTIKEY_RESOLVER::getPublicKey)
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

//            if (!Relationship.ASSERTION.getName().equals(proof.purpose())) {
//                throw new IllegalArgumentException();
//            }

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
