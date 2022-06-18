package com.apicatalog.vc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.json.JsonLdComparison;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.jsonld.loader.HttpLoader;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.ld.signature.DataError;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.ed25519.Ed25519ProofOptions2020;
import com.apicatalog.vc.api.IssuerApi;
import com.apicatalog.vc.api.Vc;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.stream.JsonGenerator;

public class VcTestRunnerJunit {

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

    public VcTestRunnerJunit(VcTestCase testCase) {
        this.testCase = testCase;
    }

    public void execute() {

        assertNotNull(testCase.type);
        assertNotNull(testCase.input);

        try {
            if (testCase.type.contains("https://github.com/filip26/iron-verifiable-credentials/tests/vocab#VeriferTest")) {

                Vc.verify(testCase.input).loader(LOADER).isValid();
                assertFalse(isNegative(), "Expected error " + testCase.result);

            } else if (testCase.type.contains("https://github.com/filip26/iron-verifiable-credentials/tests/vocab#IssuerTest")) {

                assertNotNull(testCase.result);

                //FIXME
                Ed25519ProofOptions2020 options = new Ed25519ProofOptions2020();
                options.setCreated(testCase.created);
                options.setVerificationMethod(testCase.verificationMethod);
                options.setPurpose(URI.create("https://w3id.org/security#assertionMethod"));//TODO make it configurable
                options.setDomain(testCase.domain);
                
                URI keyPairLocation = testCase.keyPair;

                if (keyPairLocation == null) {
                    // set dummy key pair
                    keyPairLocation = URI.create("https://github.com/filip26/iron-verifiable-credentials/issuer/0001-keys.json");
                }

                IssuerApi issuer = Vc
                                    .sign(testCase.input, keyPairLocation, options)
                                    .loader(LOADER)
                                    ;
                
                JsonObject signed = null;

                if (testCase.context != null) {

                    signed = issuer.getCompacted(testCase.context);

                } else {
                    signed = issuer.get();
                }

                assertFalse(isNegative(), "Expected error " + testCase.result);

                assertNotNull(signed);

//TODO  getCompacted(context)
//signed = JsonLd.compact(JsonDocument.of(signed), JsonDocument.of(new StringReader("{\"@context\":[\"https://github.com/filip26/iron-verifiable-credentials/issue/0001-context.jsonld\"]}"))).loader(LOADER).get();

                final Document expected = LOADER.loadDocument(URI.create((String)testCase.result), new DocumentLoaderOptions());

                boolean match = JsonLdComparison.equals(signed, expected.getJsonContent().orElse(null));

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

        } catch (DataError e) {
            assertException(toCode(e), e);

        } catch (JsonLdError e) {
            e.printStackTrace();
            fail(e);
        }
    }

    final static String toCode(DataError e) {
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

    public static final void write(final PrintWriter writer, final JsonWriterFactory writerFactory, final String name, final JsonValue result) {

        writer.println(name + ":");

        final StringWriter out = new StringWriter();

        try (final JsonWriter jsonWriter = writerFactory.createWriter(out)) {
            jsonWriter.write(result);
        }

        writer.write(out.toString());
        writer.println();
    }

}
