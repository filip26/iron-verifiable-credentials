package com.apicatalog.vc;

import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.vc.loader.StaticContextLoader;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

@DisplayName("Verifiable Credentials Test Suite")
@TestMethodOrder(OrderAnnotation.class)
class VcTest {

    @DisplayName("VCDM 1.1")
    @ParameterizedTest(name = "{0}")
    @MethodSource({ "vcdm11Manifest" })
    @Order(10)
    void vcdm11(VcTestCase testCase) {
        new VcTestRunnerJunit(testCase).execute();
    }

    @DisplayName("VCDM 2.0")
    @ParameterizedTest(name = "{0}")
    @MethodSource({ "vcdm20Manifest" })
    @Order(20)
    void vcdm20(VcTestCase testCase) {
        new VcTestRunnerJunit(testCase).execute();
    }

    @DisplayName("VCDI 1.0")
    @ParameterizedTest(name = "{0}")
    @MethodSource({ "vcdi10Manifest" })
    @Order(30)
    void vcdi10(VcTestCase testCase) {
        new VcTestRunnerJunit(testCase).execute();
    }

    @DisplayName("Verifier")
    @ParameterizedTest(name = "{0}")
    @MethodSource({ "verifierManifest" })
    @Order(40)
    void verifier(VcTestCase testCase) {
        new VcTestRunnerJunit(testCase).execute();
    }

    @DisplayName("Issuer")
    @ParameterizedTest(name = "{0}")
    @MethodSource({ "issuerManifest" })
    @Order(50)
    void issuer(VcTestCase testCase) {
        assumeFalse("t0005".equals(testCase.id.getFragment())); // skip require issuanceDate when issuing

        new VcTestRunnerJunit(testCase).execute();
    }

    static final Stream<VcTestCase> vcdm11Manifest() throws JsonLdError, IOException {
        return manifest("vcdm11-manifest.jsonld");
    }
    
    static final Stream<VcTestCase> vcdm20Manifest() throws JsonLdError, IOException {
        return manifest("vcdm20-manifest.jsonld");
    }

    static final Stream<VcTestCase> vcdi10Manifest() throws JsonLdError, IOException {
        return manifest("vcdi10-manifest.jsonld");
    }

    static final Stream<VcTestCase> verifierManifest() throws JsonLdError, IOException {
        return manifest("verifier-manifest.jsonld");
    }

    static final Stream<VcTestCase> issuerManifest() throws JsonLdError, IOException {
        return manifest("issuer-manifest.jsonld");
    }

    static final Stream<VcTestCase> manifest(String name) throws JsonLdError, IOException {

        try (final InputStream is = VcTest.class.getResourceAsStream(name)) {

            final JsonObject manifest = JsonLd.expand(JsonDocument.of(is))
                    .base("https://github.com/filip26/iron-verifiable-credentials/")
                    .loader(new StaticContextLoader(VcTestRunnerJunit.LOADER))
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
