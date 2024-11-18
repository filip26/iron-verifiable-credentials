package com.apicatalog.vc;

import static com.apicatalog.vcdi.DataIntegrityParam.challenge;
import static com.apicatalog.vcdi.DataIntegrityParam.domain;
import static com.apicatalog.vcdi.DataIntegrityParam.nonce;
import static com.apicatalog.vcdi.DataIntegrityParam.purpose;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.apicatalog.controller.key.KeyPair;
import com.apicatalog.controller.key.VerificationKey;
import com.apicatalog.cryptosuite.SigningError;
import com.apicatalog.cryptosuite.VerificationError;
import com.apicatalog.did.key.DidKey;
import com.apicatalog.did.key.DidKeyResolver;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.json.JsonLdComparison;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;
import com.apicatalog.linkedtree.builder.TreeBuilderError;
import com.apicatalog.linkedtree.jsonld.io.JsonLdReader;
import com.apicatalog.linkedtree.orm.mapper.TreeReaderMapping;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.vc.issuer.Issuer;
import com.apicatalog.vc.jsonld.ContextAwareReaderProvider;
import com.apicatalog.vc.loader.StaticContextLoader;
import com.apicatalog.vc.method.resolver.ControllableKeyProvider;
import com.apicatalog.vc.method.resolver.MethodPredicate;
import com.apicatalog.vc.method.resolver.MethodSelector;
import com.apicatalog.vc.method.resolver.VerificationKeyProvider;
import com.apicatalog.vc.model.generic.GenericReader;
import com.apicatalog.vc.processor.Parameter;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.reader.Reader;
import com.apicatalog.vc.verifier.Verifier;
import com.apicatalog.vcdi.DataIntegrityProofDraft;
import com.apicatalog.vcdi.DataIntegritySuite;
import com.apicatalog.vcdi.VcdiVocab;
import com.apicatalog.vcdm.VcdmVocab;
import com.apicatalog.vcdm.v11.Vcdm11Reader;
import com.apicatalog.vcdm.v20.Vcdm20Reader;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.stream.JsonGenerator;

public class VcTestRunnerJunit {

    final VcTestCase testCase;

    final static DocumentLoader LOADER = new UriBaseRewriter(VcTestCase.BASE, "classpath:",
            new SchemeRouter().set("classpath", new ClasspathLoader()));

    final static VerificationKeyProvider RESOLVERS = defaultResolvers(new StaticContextLoader((LOADER)));

    final static TestSignatureSuite TEST_DI_SUITE = (new TestSignatureSuite());

    final static Verifier VERIFIER = Verifier
            .with(TEST_DI_SUITE)
            .loader(LOADER)
            .methodResolver(RESOLVERS)
            .modelProvider(proofAdapter -> {
                Vcdm11Reader vcdm11 = Vcdm11Reader.with(proofAdapter);

                return new ContextAwareReaderProvider()
                        .with(VcdmVocab.CONTEXT_MODEL_V1, vcdm11)
                        .with(VcdmVocab.CONTEXT_MODEL_V2, Vcdm20Reader.with(proofAdapter)
                                // add VCDM 1.1 credential support
                                .v11(vcdm11))
                        .with(VcdiVocab.CONTEXT_MODEL_V2, GenericReader.with(proofAdapter));
            });;

    final static Reader READER = Reader.with(TEST_DI_SUITE, DataIntegritySuite.generic()).loader(LOADER);

    public VcTestRunnerJunit(VcTestCase testCase) {
        this.testCase = testCase;
    }

    public void execute() {

        assertNotNull(testCase.type);
        assertNotNull(testCase.input);

        try {
            if (testCase.type.contains(VcTestCase.vocab("VeriferTest"))) {

                final Verifiable verifiable = VERIFIER.verify(testCase.input,
                        challenge(testCase.challenge),
                        purpose(testCase.purpose),
                        domain(testCase.domain),
                        nonce(testCase.nonce)
                        );

                assertFalse(isNegative(), "Expected error " + testCase.result);

                assertNotNull(verifiable);

            } else if (testCase.type.contains(VcTestCase.vocab("IssuerTest"))) {

                assertNotNull(testCase.result);

                URI keyPairLocation = testCase.keyPair;

                if (keyPairLocation == null) {
                    // set dummy key pair
                    keyPairLocation = URI.create(VcTestCase.base("method/multikey-pair.json"));
                }

                final Issuer issuer = TEST_DI_SUITE.createIssuer(getKeys(keyPairLocation, LOADER))
                        .loader(LOADER);

                URI purpose = testCase.purpose;

                if (purpose == null) {
                    purpose = URI.create("https://w3id.org/security#assertionMethod");
                }

                // proof draft
                final DataIntegrityProofDraft draft = TEST_DI_SUITE.createDraft(
                        testCase.verificationMethod,
                        purpose);

                draft.created(testCase.created);
                draft.domain(testCase.domain);
                draft.challenge(testCase.challenge);
                draft.nonce(testCase.nonce);
                draft.id(testCase.proofId);
                draft.previousProof(testCase.previousProof);

                final JsonObject signed = issuer.sign(testCase.input, draft);

                assertFalse(isNegative(), "Expected error " + testCase.result);

                assertNotNull(signed);

                final Document expected = LOADER.loadDocument(URI.create((String) testCase.result),
                        new DocumentLoaderOptions());

                boolean match = JsonLdComparison.equals(signed,
                        expected.getJsonContent().orElse(null));

                if (!match) {

                    write(testCase, signed, expected.getJsonContent().orElse(null));

                    fail("Expected result does not match");
                }

            } else if (testCase.type.contains(VcTestCase.vocab("ValidationTest"))) {

                Verifiable verifiable = READER.read(testCase.input);
                assertNotNull(verifiable);

                verifiable.validate();

                assertNotNull(verifiable.proofs());
                assertTrue(verifiable.proofs().isEmpty());

            } else if (testCase.type.contains(VcTestCase.vocab("ProofValidationTest"))) {

                Verifiable verifiable = READER.read(testCase.input);
                assertNotNull(verifiable);

                assertNotNull(verifiable.proofs());
                assertFalse(verifiable.proofs().isEmpty());

                for (Proof proof : verifiable.proofs()) {
                    proof.validate(toMap(
                            challenge(testCase.challenge),
                            purpose(testCase.purpose),
                            domain(testCase.domain),
                            nonce(testCase.nonce)));
                }

            } else {
                fail("Unknown test execution method: " + testCase.type);
                return;
            }

            if (testCase.type.stream().noneMatch(o -> o.endsWith("PositiveEvaluationTest"))) {
                fail();
                return;
            }

        } catch (VerificationError e) {
            assertException(e.verificationErrorCode() != null ? e.verificationErrorCode().name() : null, e);

        } catch (SigningError e) {
            assertException(e.signatureErrorCode() != null ? e.signatureErrorCode().name() : null, e);

        } catch (DocumentError e) {
            assertException(e.code(), e);

        } catch (JsonLdError e) {
            e.printStackTrace();
            fail(e);
        } catch (Exception e) {
            assertException(e.getClass().getSimpleName(), e);
        }
    }

    final void assertException(final String code, Throwable e) {

        if (!isNegative()) {
            e.printStackTrace();
            fail(e.getMessage(), e);
            return;
        }

        if (!Objects.equals(testCase.result, code)) {
            e.printStackTrace();
        }

        // compare expected exception
        assertEquals(testCase.result, code);
    }

    final boolean isNegative() {
        return testCase.type.stream().anyMatch(o -> o.endsWith("NegativeEvaluationTest"));
    }

    public static void write(final VcTestCase testCase, final JsonStructure result,
            final JsonStructure expected) {
        final StringWriter stringWriter = new StringWriter();

        try (final PrintWriter writer = new PrintWriter(stringWriter)) {
            writer.println("Test " + testCase.id + ": " + testCase.name);

            final JsonWriterFactory writerFactory = Json.createWriterFactory(
                    Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true));

            if (expected != null) {
                write(writer, writerFactory, "Expected", expected);
                writer.println();
            }

            if (result != null) {
                write(writer, writerFactory, "Actual", result);
                writer.println();
            }
        }

        System.out.println(stringWriter.toString());
    }

    static final void write(final PrintWriter writer, final JsonWriterFactory writerFactory,
            final String name, final JsonValue result) {

        writer.println(name + ":");

        final StringWriter out = new StringWriter();

        try (final JsonWriter jsonWriter = writerFactory.createWriter(out)) {
            jsonWriter.write(result);
        }

        writer.write(out.toString());
        writer.println();
    }

    static final KeyPair getKeys(final URI keyPairLocation, final DocumentLoader loader)
            throws DocumentError, JsonLdError, TreeBuilderError, NodeAdapterError {

        final DocumentLoader docLoader = new StaticContextLoader(loader);

        final JsonObject keys = docLoader.loadDocument(keyPairLocation, new DocumentLoaderOptions()).getJsonContent().map(JsonStructure::asJsonObject).orElseThrow();

        JsonLdReader reader = JsonLdReader.of(TreeReaderMapping.createBuilder()
                .scan(TestMultikey.class).build(), docLoader);

        return reader.read(TestMultikey.class, keys);
    }

    static final VerificationKeyProvider defaultResolvers(DocumentLoader loader) {

        byte[] wellKnownPublicKey = TestMulticodecKeyMapper.PUBLIC_KEY_CODEC.decode(
                Multibase.BASE_58_BTC.decode("z5C4SPo8HWx5EYE7nLGYfRqUcPG4eN6vezEsobV622h2danE"));

        return MethodSelector.create()
                // accept specific method id
                .with(MethodPredicate.methodId(
                        URI.create("https://github.com/filip26/iron-verifiable-credentials/method/multikey-public.jsonld")::equals),
                        new RemoteTestMultiKeyProvider(loader))

                // accept embedded method when public key is well-known and trusted
                .with(MethodPredicate.methodType(VerificationKey.class)
                        .and(proof -> Arrays.equals(
                                wellKnownPublicKey,
                                ((VerificationKey) proof.method()).publicKey().rawBytes()))
                        ,
                        // just pass the embedded key back
                        proof -> (VerificationKey) proof.method())
                
                // accept did:key
                .with(MethodPredicate.methodId(DidKey::isDidKeyUrl),
                        ControllableKeyProvider.of(new DidKeyResolver(TestMulticodecKeyMapper.CODECS)))
                
                .with(i -> true, proof -> {
                    System.out.println("An unknown method");
                    System.out.println(proof.method());
                    return null;
                })
                .build();

//        Collection<VerificationKeyProvider> resolvers = new LinkedHashSet<>();
//        resolvers.add(new DidKeyMethodResolver(TestAlgorithm.DECODER));
        // return resolvers;
    }

    static final Map<String, Object> toMap(Parameter<?>... parameters) {
        return parameters != null && parameters.length > 0
                ? Stream.of(parameters)
                        .filter(p -> p.name() != null && p.value() != null)
                        .collect(Collectors.toMap(
                                Parameter::name,
                                Parameter::value))
                : Collections.emptyMap();
    }

}
