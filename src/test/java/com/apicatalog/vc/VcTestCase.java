package com.apicatalog.vc;

import static org.junit.jupiter.api.Assertions.fail;

import java.net.URI;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.lds.DataError;
import com.apicatalog.lds.ed25519.Ed25519VerificationKey2020;
import com.apicatalog.lds.proof.VerificationMethod;
import com.apicatalog.lds.proof.VerificationMethodReference;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public class VcTestCase {

    public URI id;

    public String name;

    public URI input;

    public Set<String> type;

    public Object result;

    public URI keyPair;

    public VerificationMethod verificationMethod;

    public Instant created;

    public URI context;

    public static VcTestCase of(JsonObject test, JsonObject manifest, DocumentLoader loader) {

        final VcTestCase testCase = new VcTestCase();

        testCase.id = URI.create(test.getString(Keywords.ID));

        testCase.type = test.getJsonArray(Keywords.TYPE).stream().map(JsonString.class::cast).map(JsonString::getString)
                .collect(Collectors.toSet());

        testCase.name = test.getJsonArray("http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#name")
                .getJsonObject(0).getString(Keywords.VALUE);

        testCase.input = URI.create(test.getJsonArray("http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#action")
                .getJsonObject(0).getString(Keywords.ID));

        if (test.containsKey("https://github.com/filip26/iron-verifiable-credentials/tests/vocab#context")) {
            testCase.context = URI.create(
                    test.getJsonArray("https://github.com/filip26/iron-verifiable-credentials/tests/vocab#context")
                            .getJsonObject(0).getString(Keywords.ID));
        }

        if (test.containsKey("http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#result")) {
            final JsonObject result = test
                    .getJsonArray("http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#result").getJsonObject(0);

            JsonValue resultValue = result.getOrDefault(Keywords.ID, result.getOrDefault(Keywords.VALUE, null));

            if (JsonUtils.isString(resultValue)) {
                testCase.result = ((JsonString) resultValue).getString();

            } else {
                testCase.result = !JsonValue.ValueType.FALSE.equals(resultValue.getValueType());
            }
        }

        if (test.containsKey("https://github.com/filip26/iron-verifiable-credentials/tests/vocab#options")) {

            JsonObject options = test
                    .getJsonArray("https://github.com/filip26/iron-verifiable-credentials/tests/vocab#options")
                    .getJsonObject(0);

            testCase.keyPair = URI.create(
                    options.getJsonArray("https://github.com/filip26/iron-verifiable-credentials/tests/vocab#keyPair")
                            .getJsonObject(0).getString(Keywords.ID));

            JsonObject method = options
                    .getJsonArray(
                            "https://github.com/filip26/iron-verifiable-credentials/tests/vocab#verificationMethod")
                    .getJsonObject(0);

            if (JsonLdUtils.isTypeOf("https://w3id.org/security#Ed25519VerificationKey2020", method)) {
                try {
                    testCase.verificationMethod = Ed25519VerificationKey2020.from(method);

                } catch (DataError e) {
                    fail(e);
                }

            } else {
                JsonLdUtils.getId(method)
                        .ifPresent(id -> testCase.verificationMethod = new VerificationMethodReference(id));
            }

            if (options.containsKey("https://github.com/filip26/iron-verifiable-credentials/tests/vocab#created")) {
                testCase.created = Instant.parse(options
                        .getJsonArray("https://github.com/filip26/iron-verifiable-credentials/tests/vocab#created")
                        .getJsonObject(0).getString(Keywords.VALUE));
            }

        }

        return testCase;
    }

    @Override
    public String toString() {
        return id.getFragment() + ": " + name;
    }
}
