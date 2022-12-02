package com.apicatalog.vc;

import static com.apicatalog.ld.schema.LdSchema.id;
import static com.apicatalog.ld.schema.LdSchema.multibase;
import static com.apicatalog.ld.schema.LdSchema.object;
import static com.apicatalog.ld.schema.LdSchema.property;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.Collections;
import java.util.Objects;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.json.JsonLdComparison;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.jsonld.loader.HttpLoader;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.schema.LdValueAdapter;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.ld.signature.method.VerificationMethod;
import com.apicatalog.ld.signature.proof.ProofOptions;
import com.apicatalog.multibase.Multibase.Algorithm;
import com.apicatalog.multicodec.Multicodec.Codec;
import com.apicatalog.vc.integrity.DataIntegrityKeysAdapter;
import com.apicatalog.vc.integrity.DataIntegritySchema;
import com.apicatalog.vc.processor.Issuer;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.stream.JsonGenerator;

public class VcTestRunnerJunit {

	private final VcTestCase testCase;

	public final static DocumentLoader LOADER = new UriBaseRewriter(VcTestCase.BASE, "classpath:",
			new SchemeRouter()
			        .set("http", HttpLoader.defaultInstance())
					.set("https", HttpLoader.defaultInstance())
					.set("classpath", new ClasspathLoader()));

	public VcTestRunnerJunit(VcTestCase testCase) {
		this.testCase = testCase;
	}

	public void execute() {

		assertNotNull(testCase.type);
		assertNotNull(testCase.input);

		try {
			if (testCase.type.contains(VcTestCase.vocab("VeriferTest"))) {

				Vc.verify(testCase.input, new TestSignatureSuite())
					.loader(LOADER)
                    .param("domain", testCase.domain)
					.isValid();
				
				assertFalse(isNegative(), "Expected error " + testCase.result);

			} else if (testCase.type.contains(VcTestCase.vocab("IssuerTest"))) {

				assertNotNull(testCase.result);

				URI keyPairLocation = testCase.keyPair;

				if (keyPairLocation == null) {
					// set dummy key pair
					keyPairLocation = URI.create(VcTestCase.base("issuer/0001-keys.json"));
				}
				
				final TestSignatureSuite suite = new TestSignatureSuite();

				final ProofOptions options = suite.createOptions()
                        // proof options
                        .verificationMethod(testCase.verificationMethod)
                        .purpose(URI.create("https://w3id.org/security#assertionMethod"))
                        .created(testCase.created)
                        .domain(testCase.domain)
                        ;

				final Issuer issuer = Vc.sign(testCase.input, getKeys(keyPairLocation, LOADER), options)
						                  .loader(LOADER);

				JsonObject signed = null;

				if (testCase.context != null) {

					signed = issuer.getCompacted(testCase.context);

				} else {
					signed = issuer.getExpanded();
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

	static final KeyPair getKeys(URI keyPairLocation, DocumentLoader loader)
			throws DocumentError, JsonLdError {

		final JsonArray keys = JsonLd.expand(keyPairLocation).loader(loader).get();

		for (final JsonValue key : keys) {

			if (JsonUtils.isNotObject(key)) {
				continue;
			}

			LdValueAdapter<JsonValue, VerificationMethod> adapter = object(
                    new DataIntegrityKeysAdapter(),
                    id(),
                    property(DataIntegritySchema.MULTIBASE_PUB_KEY, multibase(Algorithm.Base58Btc, Codec.Ed25519PublicKey)),
                    property(DataIntegritySchema.MULTIBASE_PRIV_KEY, multibase(Algorithm.Base58Btc, Codec.Ed25519PrivateKey))
			        );
			
			return (KeyPair) adapter.read(key);

		}
		throw new IllegalStateException();
	}

}
