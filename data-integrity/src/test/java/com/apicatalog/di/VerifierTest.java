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
import com.apicatalog.di.suite.CryptoSuites;
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
import com.apicatalog.trust.model.GraphModel;
import com.apicatalog.trust.model.GraphModel.Canonizer;
import com.apicatalog.trust.model.GraphModel.QuadConsumer;
import com.apicatalog.trust.model.Model;
import com.apicatalog.trust.model.ModelResolver;
import com.apicatalog.trust.proof.Proof;
import com.apicatalog.trust.proof.ProofGraphCursor;
import com.apicatalog.trust.proof.ProofMapCursor;
import com.fasterxml.jackson.core.JsonFactory;

public class VerifierTest {

    static Model MODEL_1 = DataIntegrity.newTypeModelBuilder("JCS")
            .proof(CryptoSuites.EDDSA_JCS_2022)
            .proof(CryptoSuites.ECDSA_JCS_2019_P256)
            .proof(CryptoSuites.ECDSA_JCS_2019_P384)
            .proof(CryptoSuites.MLDSA44_JCS_2024)
            .proof(CryptoSuites.SLHDSA128_JCS_2024)
            .c14n(Jcs::canonize)
            .processor(ProofMapCursor::new)
            .build();

    static Model MODEL_2 = DataIntegrity.newGraphModelBuilder("RDFC", VerifierTest::newRdfc)
            .proof(CryptoSuites.EDDSA_RDFC_2022)
            .proof(CryptoSuites.ECDSA_RDFC_2019_P256)
            .proof(CryptoSuites.ECDSA_RDFC_2019_P384)
            .proof(CryptoSuites.MLDSA44_RDFC_2024)
            .proof(CryptoSuites.SLHDSA128_RDFC_2024)
            .proof(Ed25519Signature2020::newReader)
            .tordf(VerifierTest::tordfc)
            .processor(ProofGraphCursor::new)
            .build();

    static ModelResolver MODEL_RESOLVER = ModelResolver.newBuilder()
            // accept any context - for test purposes only
            .model(Predicate.not(Collection::isEmpty), MODEL_1, MODEL_2)
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

        var codec = MulticodecDecoder.getInstance().getCodec(key).orElseThrow();

        // TODO check the key codec vs proof.signature().algorithm()

        return codec.decode(key);

    };

    static ProofVerifier PROOF_VERIFIER = ProofVerifier.newBuilder()
            .proof(DataIntegrityProof.TYPE_NAME)
            // TODO allow list concrete DI cryptosuites only OR list models and
            // configurations
            .proof(Ed25519Signature2020.TYPE_NAME)
            .resolver(DID_KEY_RESOLVER)
            .verifier("Ed25519", BCEd25519Verifier.getInstance()::verify)
            .verifier("P-256", BCECDSAVerifier.getP256Instance()::verify)
            .verifier("P-384", BCECDSAVerifier.getP384Instance()::verify)
            .verifier("ML-DSA-44", BCMLDSAVerifier.get44Instance()::verify)
            .verifier("SLH-DSA-SHA2-128s", BCSLHDSAVerifier.get128SInstance()::verify)
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

            var cursor = model.createCursor(contexts, signed);

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

                if (cursor.isUnknown()) {
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

    static final void tordfc(Map<String, Object> document, final GraphModel.QuadConsumer consumer) {
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

    static final RdfcPrcessor newRdfc() {
        return new RdfcPrcessor(); // TODO reuse one instance across
    }

    static class RdfcPrcessor implements Canonizer {

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final RdfCanon canon = RdfCanon.create("SHA-256");

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
            return new GraphModel.QuadConsumer() {
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
