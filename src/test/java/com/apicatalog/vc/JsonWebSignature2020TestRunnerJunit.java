package com.apicatalog.vc;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonLdComparison;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.jsonld.loader.HttpLoader;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.jws.JsonWebKeyPair2020Adapter;
import com.apicatalog.ld.signature.jws.JsonWebProof2020Adapter;
import com.apicatalog.ld.signature.jws.JwsEmbeddedProofAdapter;
import com.apicatalog.ld.signature.jws.JwsKeyPair;
import com.apicatalog.ld.signature.proof.ProofOptions;
import com.apicatalog.vc.processor.JwsIssuer;
import com.apicatalog.vc.processor.JwsVerifier;
import jakarta.json.*;
import jakarta.json.stream.JsonGenerator;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test runner
 *
 * @author petr apeltauer, KAPRION Technologies GmbH
 */
public class JsonWebSignature2020TestRunnerJunit {

    private final VcTestCase testCase;

    public final static DocumentLoader LOADER =
            new UriBaseRewriter(
                    "https://github.com/filip26/iron-verifiable-credentials/",
                    "classpath:",
                    new SchemeRouter()
                    .set("http", HttpLoader.defaultInstance())
                    .set("https", HttpLoader.defaultInstance())
                    .set("classpath", new ClasspathLoader())
                );

    public JsonWebSignature2020TestRunnerJunit(VcTestCase testCase) {
        this.testCase = testCase;
    }

    public void execute() {

        System.out.println("\n//--------------------------------------------------------//");
        System.out.println("TEST " + testCase.id);

        assertNotNull(testCase.type);
        assertNotNull(testCase.input);

        try {
            if (testCase.type.contains("https://github.com/filip26/iron-verifiable-credentials/tests/vocab#VeriferTest")) {

//                Vc.verify(testCase.input).loader(LOADER).domain(testCase.domain).isValid();
//                new TestJwsVerifier().verify(testCase.input, LOADER, testCase.domain).isValid();
                new JwsVerifier(testCase.input).loader(LOADER).domain(testCase.domain).isValid();

                assertFalse(isNegative(), "Expected error " + testCase.result);

            } else if (testCase.type.contains("https://github.com/filip26/iron-verifiable-credentials/tests/vocab#IssuerTest")) {

                assertNotNull(testCase.result);

                ProofOptions options =
                        ProofOptions
                                .create(
                                        JsonWebProof2020Adapter.TYPE,
                                        testCase.verificationMethod,
                                        URI.create("https://w3id.org/security#assertionMethod")
                                )
                                .created(testCase.created)
                                .domain(testCase.domain);

                URI keyPairLocation = testCase.keyPair;

                if (keyPairLocation == null) {
                    // set dummy key pair
                    keyPairLocation = URI.create("https://github.com/filip26/iron-verifiable-credentials/jws_issuer/0001-keys.json");
                }

//                Issuer issuer = Vc
//                        .sign(testCase.input, getKeys(keyPairLocation, LOADER), options).loader(LOADER);
//                TestJwsIssuer issuer = new TestJwsIssuer()
//                        .sign(testCase.input, getKeys(keyPairLocation, LOADER), options, LOADER);
                JwsIssuer issuer = new JwsIssuer(testCase.input, getKeys(keyPairLocation, LOADER), options);
                issuer.loader(LOADER);

                JsonObject signed = null;

                if (testCase.context != null) {

                    signed = issuer.getCompacted(testCase.context);

                } else {
                    signed = issuer.getExpanded();
                }

                assertFalse(isNegative(), "Expected error " + testCase.result);

                assertNotNull(signed);

                final Document expected = LOADER.loadDocument(URI.create((String)testCase.result), new DocumentLoaderOptions());

                // Compare expected result with actual result BUT WITHOUT the "jws" signature!
                // The "jws" is variable. So it cannot be compared to a hardcoded data.
                // (JWS is always different even when signature inputs don't change.)

                JsonObject signedWithRemovedJws = getActualDocumentWithoutJws(issuer.getExpanded(), testCase);

                boolean match = JsonLdComparison.equals(signedWithRemovedJws, expected.getJsonContent().orElse(null));

                if (!match) {

                    write(testCase, signedWithRemovedJws, expected.getJsonContent().orElse(null));

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
            assertException(toCode(e), e);

        } catch (JsonLdError e) {
            e.printStackTrace();
            fail(e);
        }
    }

    final static String toCode(DocumentError e) {
        final StringBuilder sb = new StringBuilder();
        if (e.getType() != null) {
            sb.append(e.getType().name());
        }
        if (e.getSubject() != null) {

            int index = (e.getSubject().startsWith("@")) ? 1 : 0;

            sb.append(Character.toUpperCase(e.getSubject().charAt(index)));
            sb.append(e.getSubject().substring(index + 1));
        }
        if (e.getAttibutes() != null) {

            Arrays.stream(e.getAttibutes())
                .forEach(attribute -> {
                    int index = (attribute.startsWith("@")) ? 1 : 0;

                    sb.append(Character.toUpperCase(attribute.charAt(index)));
                    sb.append(attribute.substring(index + 1));
                });
        }
        return sb.toString();
    }

    final void assertException(final String code, Throwable e) {

        if (!isNegative()) {
            e.printStackTrace();
            fail(e);
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

    public static void write(final VcTestCase testCase, final JsonStructure result, final JsonStructure expected) {
        final StringWriter stringWriter = new StringWriter();

        try (final PrintWriter writer = new PrintWriter(stringWriter)) {
            writer.println("Test " + testCase.id + ": " + testCase.name);

            final JsonWriterFactory writerFactory = Json.createWriterFactory(Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true));

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

    static final void write(final PrintWriter writer, final JsonWriterFactory writerFactory, final String name, final JsonValue result) {

        writer.println(name + ":");

        final StringWriter out = new StringWriter();

        try (final JsonWriter jsonWriter = writerFactory.createWriter(out)) {
            jsonWriter.write(result);
        }

        writer.write(out.toString());
        writer.println();
    }

    static final JwsKeyPair getKeys(URI keyPairLocation, DocumentLoader loader) throws DocumentError, JsonLdError {

        final JsonArray keys = JsonLd.expand(keyPairLocation).loader(loader).get();

        for (final JsonValue key : keys) {

            if (JsonUtils.isNotObject(key)) {
                continue;
            }

            return (JwsKeyPair)(new JsonWebKeyPair2020Adapter()).deserialize(key.asJsonObject());
        }
        throw new IllegalStateException();
    }

    private static JsonObject getActualDocumentWithoutJws(JsonObject signedExpanded, VcTestCase testCase) throws JsonLdError {
        // Expected result with actual result must be compared WITHOUT the "jws" signature!
        // The "jws" is variable. So it cannot be compared to a hardcoded data.
        // (JWS is always different even when inputs don't change.)

        JsonArray proofArray = signedExpanded.getJsonArray("https://w3id.org/security#proof");
        JsonObject document = JwsEmbeddedProofAdapter.removeProof(signedExpanded);
        JsonObject proofObjectWithoutJws = JwsEmbeddedProofAdapter.removeProofValue(proofArray.get(proofArray.size()-1).asJsonObject()); //remove the "jws"
        JsonObject actualWithoutJws;
        if(proofArray.size() > 1) {
            for(int i = 0; i < proofArray.size()-1; i++) {
                document = JwsEmbeddedProofAdapter.addProof(document, proofArray.get(i).asJsonObject());
            }
        }
        actualWithoutJws = JwsEmbeddedProofAdapter.addProof(document, proofObjectWithoutJws);

        if(testCase.context != null)
            actualWithoutJws = JsonLd.compact(JsonDocument.of(actualWithoutJws), testCase.context).loader(LOADER).get(); //compact the document with @context entries

        return actualWithoutJws;
    }

}
