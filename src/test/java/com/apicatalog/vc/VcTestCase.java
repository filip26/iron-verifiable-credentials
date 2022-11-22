package com.apicatalog.vc;

import static org.junit.jupiter.api.Assertions.fail;

import java.net.URI;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.proof.VerificationMethod;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public class VcTestCase {

	static final String BASE = "https://github.com/filip26/iron-verifiable-credentials/";
	
	public URI id;

	public String name;

	public URI input;

	public Set<String> type;

	public Object result;

	public URI keyPair;

	public VerificationMethod verificationMethod;

	public Instant created;

	public String domain;

	public URI context;

	public static VcTestCase of(JsonObject test, JsonObject manifest, DocumentLoader loader) {

		final VcTestCase testCase = new VcTestCase();

		testCase.id = URI.create(test.getString(Keywords.ID));

		testCase.type = test.getJsonArray(Keywords.TYPE).stream().map(JsonString.class::cast)
				.map(JsonString::getString).collect(Collectors.toSet());

		testCase.name = test.getJsonArray(vocab("name")).getJsonObject(0).getString(Keywords.VALUE);

		testCase.input = URI
				.create(test.getJsonArray(vocab("action")).getJsonObject(0).getString(Keywords.ID));

		if (test.containsKey(vocab("context"))) {
			testCase.context = URI.create(
					test.getJsonArray(vocab("context")).getJsonObject(0).getString(Keywords.ID));
		}

		if (test.containsKey(vocab("result"))) {
			final JsonObject result = test.getJsonArray(vocab("result")).getJsonObject(0);

			JsonValue resultValue = result.getOrDefault(Keywords.ID,
					result.getOrDefault(Keywords.VALUE, null));

			if (JsonUtils.isString(resultValue)) {
				testCase.result = ((JsonString) resultValue).getString();

			} else {
				testCase.result = !JsonValue.ValueType.FALSE.equals(resultValue.getValueType());
			}
		}

		if (test.containsKey(vocab("options"))) {

			final JsonObject options = test.getJsonArray(vocab("options")).getJsonObject(0);

			if (options.containsKey(vocab("keyPair"))) {
				testCase.keyPair = URI.create(options.getJsonArray(vocab("keyPair"))
						.getJsonObject(0).getString(Keywords.ID));
			}

			if (options.containsKey(vocab("verificationMethod"))) {

				final JsonObject method = options.getJsonArray(vocab("verificationMethod")).getJsonObject(0);

//				if (JsonLdUtils.isTypeOf(vocab("TestVerificationKey2022"), method)) {
//
//				} else {
//					method = J
//					JsonLdUtils.getId(method).ifPresent(
//							id -> testCase.verificationMethod = new vocab(id, null, null, null));
//				}
				
				try {
					testCase.verificationMethod = (new TestVerificationMethodAdapter()).deserialize(method);

				} catch (DocumentError e) {
					fail(e);
				}

			}

			if (options.containsKey(vocab("created"))) {
				testCase.created = Instant.parse(options.getJsonArray(vocab("created"))
						.getJsonObject(0).getString(Keywords.VALUE));
			}

			if (options.containsKey(vocab("domain"))) {
				testCase.domain = options.getJsonArray(vocab("domain")).getJsonObject(0)
						.getString(Keywords.VALUE);
			}
		}

		return testCase;
	}

	@Override
	public String toString() {
		return id.getFragment() + ": " + name;
	}

	static String base(String url) {
		return BASE.concat(url);
	}
	
	static String vocab(String term) {
		return base("tests/vocab#").concat(term);
	}
}
