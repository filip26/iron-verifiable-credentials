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

    @DisplayName("VCDM 1.1 Validation")
    @ParameterizedTest(name = "{0}")
    @MethodSource({ "vcdm11Validation" })
    @Order(1)
    void validate(VcTestCase testCase) {
        new VcTestRunnerJunit(testCase).execute();
    }

    
    @DisplayName("VCDM 1.1 Verifier")
    @ParameterizedTest(name = "{0}")
    @MethodSource({ "vcdm11Verifier" })
    @Order(2)
    void verify(VcTestCase testCase) {
        new VcTestRunnerJunit(testCase).execute();
    }

//    @DisplayName("VCDM 2.0 Reader")
//    @ParameterizedTest(name = "{0}")
//    @MethodSource({ "vcdm20ReaderManifest" })
//    @Order(3)
//    void vcdm20Reader(VcTestCase testCase) {
//        new VcTestRunnerJunit(testCase).execute();
//    }

    @DisplayName("VCDM 2.0 Validation")
    @ParameterizedTest(name = "{0}")
    @MethodSource({ "vcdm20Validation" })
    @Order(3)
    void vcdm20Validation(VcTestCase testCase) {
        new VcTestRunnerJunit(testCase).execute();
    }

    @DisplayName("VCDM 2.0 Verifier")
    @ParameterizedTest(name = "{0}")
    @MethodSource({ "vcdm20Verifier" })
    @Order(4)
    void vcdm20Verifier(VcTestCase testCase) {
        new VcTestRunnerJunit(testCase).execute();
    }

    @DisplayName("VCDM 1.1 Issuer")
    @ParameterizedTest(name = "{0}")
    @MethodSource({ "issuerManifest" })
    @Order(4)
    void sign(VcTestCase testCase) {
        assumeFalse("t0005".equals(testCase.id.getFragment())); // skip require issuanceDate when issuing

        new VcTestRunnerJunit(testCase).execute();
    }

    static final Stream<VcTestCase> vcdm11Validation() throws JsonLdError, IOException {
        return manifest("vcdm11-validation.jsonld");
    }
    
    static final Stream<VcTestCase> vcdm11Verifier() throws JsonLdError, IOException {
        return manifest("vcdm11-verifier.jsonld");
    }

    static final Stream<VcTestCase> vcdm20Validation() throws JsonLdError, IOException {
        return manifest("vcdm20-validation.jsonld");
    }

    static final Stream<VcTestCase> vcdm20Verifier() throws JsonLdError, IOException {
        return manifest("vcdm20-verifier.jsonld");
    }

    static final Stream<VcTestCase> vcdm20ReaderManifest() throws JsonLdError, IOException {
        return manifest("vcdm20-reader-manifest.jsonld");
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
