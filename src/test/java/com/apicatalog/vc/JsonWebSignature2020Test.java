package com.apicatalog.vc;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.KeyGenError;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.jws.*;
import com.apicatalog.ld.signature.proof.ProofOptions;
import com.apicatalog.vc.processor.JwsIssuer;
import com.apicatalog.vc.processor.JwsVerifier;
import com.nimbusds.jose.jwk.JWK;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.apicatalog.jsonld.JsonLdUtils.stringToJakartaJsonObj;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * Tester of Json Web Signature 2020 suite
 *
 * @author petr apeltauer, KAPRION Technologies GmbH
 */
@DisplayName("Json Web Signature 2020 Test Suite")
@TestMethodOrder(OrderAnnotation.class)
public class JsonWebSignature2020Test {

    private static final String notSignedVc = "" + //VC without proof
            "{\n" +
            "  \"@context\": [\n" +
            "    \"https://www.w3.org/2018/credentials/v1\",\n" +
            "    \"https://www.w3.org/2018/credentials/examples/v1\"\n" +
            "  ],\n" +
            "  \"id\": \"http://example.gov/credentials/3732\",\n" +
            "  \"type\": [\"VerifiableCredential\", \"UniversityDegreeCredential\"],\n" +
            "  \"issuer\": \"https://example.edu\",\n" +
            "  \"issuanceDate\": \"2010-01-01T19:23:24Z\",\n" +
            "  \"credentialSubject\": {\n" +
            "    \"id\": \"did:example:ebfeb1f712ebc6f1c276e12ec21\",\n" +
            "    \"degree\": {\n" +
            "      \"type\": \"BachelorDegree\",\n" +
            "      \"name\": \"Bachelor of Science and Arts\"\n" +
            "    }\n" +
            "  }" +
            "}";

    /**
     * Json Web Signature 2020 should support following signature algorithms:
     * EdDSA, ES256K, ES256, ES384, PS256.
     * <p /><p />
     * NOTE: ES256K algorithm is not supported by Java 17, only by Java 8!
     * (see <a href="https://connect2id.com/products/nimbus-jose-jwt/jca-algorithm-support#alg-support-table">supported algorithms</a>).
     * <p />
     * But user can optionally override methods
     * {@link JsonWebSignature2020#sign(PrivateKey, PublicKey, String, byte[])} and
     * {@link JsonWebSignature2020#verify(PublicKey, String, String, byte[])} to solve this.
     */
    @DisplayName("JsonWebSignature2020 Signing & Verification Test of all signature algorithms")
    @Order(1)
    @Test
    void testAllSupportedSignatureAlgorithmsForSigningAndVerification() {

        //Lets test all signature algorithms supported by JsonWebSignature2020
        List<String> signatureAlgorithmsToTest = new ArrayList<>();
        signatureAlgorithmsToTest.add("EdDSA");
        signatureAlgorithmsToTest.add("ES256K");
        signatureAlgorithmsToTest.add("ES256");
        signatureAlgorithmsToTest.add("ES384");
        signatureAlgorithmsToTest.add("PS256");
//        signatureAlgorithmsToTest.add("ES512"); //ES512 is not supported by JsonWebSignature2020!

        JsonArray context = Json.createArrayBuilder()
                .add("https://www.w3.org/2018/credentials/v1")
                .add("https://www.w3.org/2018/credentials/examples/v1")
                .add("https://w3id.org/security/suites/jws-2020/v1")
                .build();

        JsonObject input = stringToJakartaJsonObj(notSignedVc);

        ProofOptions options = ProofOptions.create(
                        "https://w3id.org/security#JsonWebSignature2020",  //"type":"JsonWebSignature2020"
                        new JwsVerificationMethod(                              //"verificationMethod":"did:example:123456#keys-0"
                                URI.create("did:example:123456#keys-0")
                        ),
                        URI.create("https://w3id.org/security#assertionMethod") //"proofPurpose":"assertionMethod"
                )
                .created(Instant.parse("2022-07-29T04:30:20.100Z")); // .created(Instant.now())

        for (String signatureAlgorithmToTest : signatureAlgorithmsToTest) {

            System.out.println("\n\n--------------------------------------------------------------");
            System.out.println("\n\n----- JsonWebSignature2020 - Testing signature algorithm: -----");
            System.out.println("\n\n-------------------------" + signatureAlgorithmToTest + "------------------------------");
            System.out.println("\n\n--------------------------------------------------------------");

            JwsSignatureSuite signatureSuite = new JsonWebSignature2020(signatureAlgorithmToTest); //JsonWebSignature2020("ES256K")
            JwsLinkedDataSignature suite = new JwsLinkedDataSignature(signatureSuite);

            try {

                JWK jwk = suite.keygen();

                JwsKeyPair keys = new JwsKeyPair();
                keys.setId(options.verificationMethod().id());
                keys.setController(options.verificationMethod().controller());
                keys.setType("https://w3id.org/security#JsonWebKey2020");
                keys.setPrivateKey(jwk);
                keys.setPublicKey(jwk.toPublicJWK());

                System.out.println("SIGNING TEST - JWK (private) = \n " + keys.getPrivateKey());

                JwsIssuer issuer = new JwsIssuer(input, keys, options);
                JsonObject signedVc = issuer.getCompacted(
                        Json.createArrayBuilder()
                                .add("https://www.w3.org/2018/credentials/v1")
                                .add("https://www.w3.org/2018/credentials/examples/v1")
                                .add("https://w3id.org/security/suites/jws-2020/v1")
                                .build()
                );

                System.out.println("SIGNING TEST - SIGNED VC = \n " + signedVc);

                System.out.println("VERIFICATION TEST - JWK (public) = \n " + keys.getPublicKey());

                JwsVerifier verifier = new JwsVerifier(signedVc, keys.getPublicKey());
                /*boolean isValid = */verifier.isValid();

            } catch (Exception | DocumentError | KeyGenError | SigningError | VerificationError e){
                if(e instanceof KeyGenError && e.getMessage().contains("secp256k1")) {
                    System.err.println("\nCurve secp256k1 (alg. ES256K) is supported on Java 8 (used by Android), but it is NOT supported on Java 17! " +
                            "(see https://connect2id.com/products/nimbus-jose-jwt/jca-algorithm-support#alg-support-table)\n");
                    e.printStackTrace();
                } else {
                    fail(e); //SHOULD NOT THROW ANY OTHER EXCEPTION (like InvalidSignature...)
                }
            }

        }

    }

    @DisplayName("Verifier")
    @ParameterizedTest(name = "{0}")
    @MethodSource({ "verifierManifest" })
    @Order(2)
    void verify(VcTestCase testCase) {

        assumeFalse("t0002".equals(testCase.id.getFragment()));       // skip "unknown proof value encoding" - there is no proofValue encoding used in JWS

        assumeFalse("t0004".equals(testCase.id.getFragment()));       // skip "invalid proof value length" - JWS length is variable

        assumeFalse("t0017".equals(testCase.id.getFragment()));       // skip "unsupported proofValue codec" - there is no codec encoding used in JWS

        new JsonWebSignature2020TestRunnerJunit(testCase).execute();
    }

    @DisplayName("Issuer")
    @ParameterizedTest(name = "{0}")
    @MethodSource({ "issuerManifest" })
    @Order(3)
    void sign(VcTestCase testCase) {

        assumeFalse("t0005".equals(testCase.id.getFragment()));       // skip require issuanceDate when issuing

        new JsonWebSignature2020TestRunnerJunit(testCase).execute();
    }

    static final Stream<VcTestCase> verifierManifest() throws JsonLdError, IOException {
        return manifest("jws_verifier-manifest.jsonld");
    }

    static final Stream<VcTestCase> issuerManifest() throws JsonLdError, IOException {
        return manifest("jws_issuer-manifest.jsonld");
    }

    static final Stream<VcTestCase> manifest(String name) throws JsonLdError, IOException {

        try (final InputStream is = VcTest.class.getResourceAsStream(name)) {

            final JsonObject manifest = JsonLd.expand(JsonDocument.of(is))
                    .base("https://github.com/filip26/iron-verifiable-credentials/")
                    .loader(JsonWebSignature2020TestRunnerJunit.LOADER)
                    .get()
                    .getJsonObject(0);

            return manifest
                    .asJsonObject().getJsonArray("http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#entries")
                    .stream()
                    .map(JsonValue::asJsonObject)
                    .map(test -> VcTestCase.of(test, manifest, JsonWebSignature2020TestRunnerJunit.LOADER));
        }
    }







}
