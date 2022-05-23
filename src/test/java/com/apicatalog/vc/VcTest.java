package com.apicatalog.vc;

import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.JsonDocument;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

class VcTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource({ "manifest" })
    void test(VcTestCase testCase) {
        
        // skip unsigned credentials
        assumeFalse("t0006".equals(testCase.id.getFragment()));
        assumeFalse("t0015".equals(testCase.id.getFragment()));
        
        new VcTestRunnerJunit(testCase).execute();
    }

    static final Stream<VcTestCase> manifest() throws JsonLdError, IOException {

        try (final InputStream is = VcTest.class.getResourceAsStream("manifest.jsonld")) {

            final JsonObject manifest = JsonLd.expand(JsonDocument.of(is))
                        .base("https://github.com/filip26/iron-verifiable-credentials/")
                        .get()
                        .getJsonObject(0);

            return manifest
                .asJsonObject().getJsonArray("http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#entries")
                .stream()
                .map(JsonValue::asJsonObject)
                .map(test -> VcTestCase.of(test, manifest));
        }
    }
}
