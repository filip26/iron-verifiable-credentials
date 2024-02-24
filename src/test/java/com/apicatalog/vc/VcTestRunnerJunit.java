package com.apicatalog.vc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.json.JsonLdComparison;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.multibase.MultibaseDecoder;
import com.apicatalog.vc.integrity.DataIntegrityProof;
import com.apicatalog.vc.integrity.DataIntegrityVocab;
import com.apicatalog.vc.issuer.IssuedVerifiable;
import com.apicatalog.vc.issuer.Issuer;
import com.apicatalog.vc.loader.StaticContextLoader;
import com.apicatalog.vc.method.MethodAdapter;
import com.apicatalog.vc.method.resolver.DidUrlMethodResolver;
import com.apicatalog.vc.method.resolver.HttpMethodResolver;
import com.apicatalog.vc.method.resolver.MethodResolver;
import com.apicatalog.vc.verifier.Verifier;

import jakarta.json.Json;
import jakarta.json.JsonArray;
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

    final static Collection<MethodResolver> RESOLVERS = defaultResolvers();
    
    final static TestSignatureSuite SUITE = (new TestSignatureSuite());

    final static Verifier VERIFIER = Verifier.with(SUITE).loader(LOADER).methodResolvers(RESOLVERS);
    
    public VcTestRunnerJunit(VcTestCase testCase) {
        this.testCase = testCase;
    }

    public void execute() {

        assertNotNull(testCase.type);
        assertNotNull(testCase.input);

        try {
            if (testCase.type.contains(VcTestCase.vocab("VeriferTest"))) {

                final Map<String, Object> params = new HashMap<>();
                params.put(DataIntegrityVocab.DOMAIN.name(), testCase.domain);
                params.put(DataIntegrityVocab.CHALLENGE.name(), testCase.challenge);
                params.put(DataIntegrityVocab.PURPOSE.name(), testCase.purpose);

                final Verifiable verifiable = VERIFIER.verify(testCase.input, params);

                assertFalse(isNegative(), "Expected error " + testCase.result);
                
                assertNotNull(verifiable);

            } else if (testCase.type.contains(VcTestCase.vocab("IssuerTest"))) {

                assertNotNull(testCase.result);

                URI keyPairLocation = testCase.keyPair;

                if (keyPairLocation == null) {
                    // set dummy key pair
                    keyPairLocation = URI.create(VcTestCase.base("issuer/0001-keys.json"));
                }

                final DataIntegrityProof draft = SUITE.createDraft(
                        // proof options
                        testCase.verificationMethod,
                        URI.create("https://w3id.org/security#assertionMethod"),
                        testCase.created,
                        testCase.domain,
                        testCase.challenge,
                        testCase.nonce
                        );

                final Issuer issuer = SUITE.createIssuer(getKeys(keyPairLocation, LOADER, draft.methodProcessor()))
                        .loader(LOADER);
                        
                final IssuedVerifiable issued = issuer.sign(testCase.input, draft);

                JsonObject signed = null;

                if (testCase.context != null) {
                    signed = issued.compacted(testCase.context);

                } else if (testCase.compacted) {
                    signed = issued.compacted();

                } else {
                    signed = issued.expanded();
                }

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

            } else {
                fail("Unknown test execution method: " + testCase.type);
                return;
            }

            if (testCase.type.stream().noneMatch(o -> o.endsWith("PositiveEvaluationTest"))) {
                fail();
                return;
            }

        } catch (VerificationError e) {
            assertException(e.getCode() != null ? e.getCode().name() : null, e);

        } catch (SigningError e) {
            assertException(e.getCode() != null ? e.getCode().name() : null, e);

        } catch (DocumentError e) {
            assertException(e.getCode(), e);

        } catch (JsonLdError e) {
            e.printStackTrace();
            fail(e);
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

    static final KeyPair getKeys(final URI keyPairLocation, final DocumentLoader loader, MethodAdapter methodAdapter)
            throws DocumentError, JsonLdError {

        final JsonArray keys = JsonLd.expand(keyPairLocation).loader(new StaticContextLoader(loader)).get();

        for (final JsonValue key : keys) {

            if (JsonUtils.isNotObject(key)) {
                continue;
            }

            return (KeyPair) methodAdapter.read(key.asJsonObject());
        }
        throw new IllegalStateException();
    }

    static final Collection<MethodResolver> defaultResolvers() {
        Collection<MethodResolver> resolvers = new LinkedHashSet<>();
        resolvers.add(new DidUrlMethodResolver(MultibaseDecoder.getInstance(), TestKeyAdapter.DECODER));
        resolvers.add(new HttpMethodResolver());
        return resolvers;
    }
}
