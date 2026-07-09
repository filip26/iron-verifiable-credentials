package com.apicatalog.di;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.apicatalog.crypto.bc.BCECDSAVerifier;
import com.apicatalog.crypto.bc.BCEd25519Verifier;
import com.apicatalog.crypto.bc.BCMLDSAVerifier;
import com.apicatalog.crypto.bc.BCSLHDSAVerifier;
import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.di.proof.Ed25519Signature2020;
import com.apicatalog.di.suite.ECDSA2019;
import com.apicatalog.di.suite.EdDSA2022;
import com.apicatalog.di.suite.MLDSA2024;
import com.apicatalog.di.suite.SLHDSA2024;
import com.apicatalog.jcs.Jcs;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.multibase.MultibaseDecoder;
import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.rdf.api.RdfQuadConsumer;
import com.apicatalog.rdf.canon.RdfCanon;
import com.apicatalog.tree.io.Tree;
import com.apicatalog.tree.io.jakcson.Jackson2Emitter;
import com.apicatalog.trust.MethodResolver;
import com.apicatalog.trust.ProofVerifier;
import com.apicatalog.trust.model.DataModel;
import com.apicatalog.trust.model.ModelResolver;
import com.apicatalog.trust.model.SematicModel;
import com.apicatalog.trust.model.SematicModel.GraphCanonizer;
import com.apicatalog.trust.model.SematicModel.QuadConsumer;
import com.apicatalog.trust.proof.GraphProofCursor;
import com.apicatalog.trust.proof.MapProofCursor;
import com.apicatalog.trust.proof.Proof;
import com.fasterxml.jackson.core.JsonFactory;

public class VerifierTest {

    static DataModel MODEL_1 = DataIntegrity.newLexicalModelBuilder(DataModel.C14N_JCS)
            .proof(EdDSA2022.withJCS())
            .proof(ECDSA2019.withJCS())
            .proof(MLDSA2024.get44withJCS())
            .proof(SLHDSA2024.get128withJCS())
            .c14n(Jcs::canonize)
            .processor(MapProofCursor::new)
            .build();

    static DataModel MODEL_2 = DataIntegrity.newSematicModelBuilder(DataModel.C14N_RDFC)
            .proof(EdDSA2022::get)
            .proof(ECDSA2019.withRDFC())
            .proof(MLDSA2024::get44)
            .proof(SLHDSA2024::get128s)
            .Ed25519Signature2020()
            .tordf(VerifierTest::toRDF)
            .c14n(VerifierTest::newRDFC)
            .processor(GraphProofCursor::new)
            .build();

    static ModelResolver MODEL_RESOLVER = ModelResolver.newBuilder()
            // accept any context - for test purposes only
            .model(Predicate.not(Collection::isEmpty),
                    MODEL_1,
                    MODEL_2)
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
            .digestFactory(Resources.DIGEST_FACTORY::get)
            .build();

    @ParameterizedTest
    @MethodSource({ "resources" })
    void testVerify(String resource) throws Throwable {

        var signed = Resources.getMap(resource);

        var contexts = MODEL_RESOLVER.getContexts(signed);

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
//                IO.println(((DataIntegrityProof)proof).cryptosuite().algorithm());
//                IO.println(new String(cursor.data().digestiblePayload().canonicalPayload()));
//                IO.println("D: " + HexFormat.of().formatHex(cursor.data().digestiblePayload().digest("SHA-384")));

//                var x = MessageDigest.getInstance("SHA-384");
//                x.update(proof.canonicalPayload());
//IO.println("P: " +  HexFormat.of().formatHex(x.digest()));
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
                .filter(name -> name.endsWith(".signed.json"))
                .sorted();
    }

    static final void toRDF(Map<String, Object> document, final SematicModel.QuadConsumer consumer) {
        try {
            // TODO temporary, remove with Titanium v2.x.x
            var bos = new ByteArrayOutputStream();
            try (var emitter = Jackson2Emitter.newEmitter(bos, JsonFactory.builder().build())) {
                Tree.write(document, emitter);
            }

            var toRdf = JsonLd.toRdf(JsonDocument.of(new ByteArrayInputStream(bos.toByteArray())))
                    .loader(ContextLoader.getInstance());

            // TODO remove with rdf-api 2.0.0
            toRdf.provide(new RdfQuadConsumer() {

                @Override
                public RdfQuadConsumer quad(String subject, String predicate, String object, String datatype,
                        String language,
                        String direction, String graph) {

                    consumer.accept(subject, predicate, object, datatype, language, direction, graph);
                    return this;
                }
            });

        } catch (IOException | JsonLdError e) {
            throw new IllegalStateException(e);
        }
    }

    static final RdfcPrcessor newRDFC() {
        return new RdfcPrcessor(); // TODO reuse one instance across
    }

    static class RdfcPrcessor implements GraphCanonizer {

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final RdfCanon canon = RdfCanon.create(Resources.DIGEST_FACTORY.get("SHA-256"));

        @Override
        public byte[] canonize() {

            bos.reset();

            canon.provide(line -> {
                try {
                    bos.write(line.getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });

            return bos.toByteArray();
        }

        @Override
        public QuadConsumer consumer() {
            // TODO remove with rdf-api 2.0.0
            return new SematicModel.QuadConsumer() {
                @Override
                public void accept(
                        String subject,
                        String predicate,
                        String object,
                        String datatype,
                        String language,
                        String direction,
                        String graph) {

                    canon.quad(subject, predicate, object, datatype, language, direction, graph);
                }
            };
        }
    }
}
