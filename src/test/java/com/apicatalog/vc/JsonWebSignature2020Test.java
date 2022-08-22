package com.apicatalog.vc;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdErrorCode;
import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.KeyGenError;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.jws.*;
import com.apicatalog.ld.signature.jws.JwsVerificationMethod;
import com.apicatalog.ld.signature.proof.ProofOptions;
import com.apicatalog.vc.loader.StaticContextLoader;
import com.nimbusds.jose.jwk.JWK;
import jakarta.json.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

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


        for (String signatureAlgorithmToTest : signatureAlgorithmsToTest) {

            ////////////////////////////////////////////////////////////////////////////////////////////////////
            //Code below is based on com.apicatalog.vc.processor.Issuer and com.apicatalog.vc.processor.Verifier
            ////////////////////////////////////////////////////////////////////////////////////////////////////

            System.out.println("\n\n--------------------------------------------------------------");
            System.out.println("\n\n----- JsonWebSignature2020 - Testing signature algorithm: -----");
            System.out.println("\n\n-------------------------" + signatureAlgorithmToTest + "------------------------------");
            System.out.println("\n\n--------------------------------------------------------------");

            JsonObject docToSign = stringToJakartaJsonObj(notSignedVc);
            DocumentLoader contextLoader = SchemeRouter.defaultInstance();
            contextLoader = new StaticContextLoader(contextLoader); //https://w3id.org/security/suites/jws-2020/v1 is now in StaticContextLoader

            //Proof json obj. without the signature
            ProofOptions options = ProofOptions.create(
                            "https://w3id.org/security#JsonWebSignature2020",  //"type":"JsonWebSignature2020"
                            new JwsVerificationMethod(                                     //"verificationMethod":"https://example.edu/issuers/14#key-1"
                                    URI.create("did:example:123456#keys-0")
                            ),
                            URI.create("https://w3id.org/security#assertionMethod") //"proofPurpose":"assertionMethod"
                    )
                    // .created(Instant.now())
                    .created(Instant.parse("2022-07-29T04:30:20.100Z"));

            try {

                System.out.println("TEST - testJsonWebSignature2020 started");

                //////////////////////////////////
                ///////////// SIGNING ////////////
                //////////////////////////////////

                // load and expand the document ("not signed VC") with JSON-LD entries in @context field
                JsonArray expanded = JsonLd.expand(JsonDocument.of(docToSign)).loader(contextLoader).get();
                JsonObject firstObj = JsonLdUtils
                        .findFirstObject(expanded)
                        .orElseThrow(() ->
                                new DocumentError(DocumentError.ErrorType.Invalid, "document")
                        );

                JwsSignatureSuite signatureSuite = new JsonWebSignature2020(signatureAlgorithmToTest); //JsonWebSignature2020("ES256K")

                JsonObject data = JwsEmbeddedProofAdapter.removeProof(firstObj);

                JwsLinkedDataSignature suite = new JwsLinkedDataSignature(signatureSuite);

                JWK jwk = suite.keygen();
                //JWK can be also loaded from string like this:
//              JWK jwk = JWK.parse("{\"kty\":\"EC\",\"crv\":\"secp256k1\",\"x\":\"GBMxavme-AfIVDKqI6WBJ4V5wZItsxJ9muhxPByllHQ\",\"y\":\"SChlfVBhTXG_sRGc9ZdFeCYzI3Kbph3ivE12OFVk4jo\"}");

                System.out.println("SIGNING TEST - JWK (private) = \n " + jwk.toString());

                JsonObject proof = signatureSuite.getProofAdapter().serialize(JsonWebSignature2020.toUnsignedJwsProof(options)); //uses JsonWebProof2020Adapter#serialize()

//                KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA"); //when using java.security
//                gen.initialize(2048);
//                KeyPair keyPair = gen.generateKeyPair();
//                String jws = suite.sign(data, keyPair.getPrivate(), keyPair.getPublic(), "123", proof);
                String jws = suite.sign(data, jwk, proof);

                proof = signatureSuite.getProofAdapter().setProofValue(proof, jws); //uses JsonWebProof2020Adapter#setProofValue()

                JsonObject expandedSignedVc = JwsEmbeddedProofAdapter.addProof(firstObj, proof);

                String expandedSignedVcString = jakartaJsonObjToString(expandedSignedVc);
                System.out.println("SIGNING TEST - EXPANDED SIGNED VC = \n " +expandedSignedVcString);

                JsonArray context = Json.createArrayBuilder()
                        .add("https://www.w3.org/2018/credentials/v1")
                        .add("https://www.w3.org/2018/credentials/examples/v1")
                        .add("https://w3id.org/security/suites/jws-2020/v1")
                        .build();

                String signedVcString;
                try {
                    //might require online connection if the needed linked data documents are not stored offline in contextLoader
                    JsonObject compactedSignedVc = JsonLd.compact(JsonDocument.of(expandedSignedVc), JsonDocument.of(context)).loader(contextLoader).get();
                    String compactedSignedVcString = jakartaJsonObjToString(compactedSignedVc);
                    System.out.println("SIGNING TEST - COMPACTED SIGNED VC = \n " + compactedSignedVcString);
                    signedVcString = compactedSignedVcString;
                } catch (Exception e) {
                    //thrown if online connection is not available or linked data document cannot be found online
                    e.printStackTrace();
                    System.out.println("SIGNING TEST - CANNOT COMPACT THE VC");
                    signedVcString = expandedSignedVcString;
                }



                //////////////////////////////////
                ////////// VERIFICATION //////////
                //////////////////////////////////

                JWK pubJwk = jwk.toPublicJWK();
                JsonObject docToVerify = stringToJakartaJsonObj(signedVcString);
                // load and expand the document (signed VC) with JSON-LD entries in @context field
                JsonArray expanded2 = JsonLd.expand(JsonDocument.of(docToVerify)).loader(contextLoader).get();

                for (JsonValue item : expanded2) {
                    if (JsonUtils.isNotObject(item)) {
                        throw new DocumentError(DocumentError.ErrorType.Invalid, "document");
                    }
                    JsonObject currObj = item.asJsonObject();
                    System.out.println("VERIFICATION TEST - EXPANDED SIGNED VC = \n " + currObj);

                    Collection<JsonValue> proofs = JwsEmbeddedProofAdapter.getProof(currObj);
                    if (proofs == null || proofs.isEmpty())
                        throw new DocumentError(DocumentError.ErrorType.Missing, "proof");

                    JsonObject data2 = JwsEmbeddedProofAdapter.removeProof(currObj);

                    // verify attached proofs' signatures
                    for (JsonValue proofValue : proofs) {

                        if (JsonUtils.isNotObject(proofValue))
                            throw new DocumentError(DocumentError.ErrorType.Invalid, "proof");

                        final Collection<String> proofType = JsonLdUtils.getType(proofValue.asJsonObject());
                        if (proofType == null || proofType.isEmpty())
                            throw new DocumentError(DocumentError.ErrorType.Missing, "proof", Keywords.TYPE);

                        System.out.println("VERIFICATION TEST - JWK (public) = \n " + pubJwk);

                        String algorithm = getSignatureAlgorithmFromJwk(pubJwk);

                        JwsSignatureSuite signatureSuite2 = new JsonWebSignature2020(algorithm);

                        JwsProof proof2 = signatureSuite2.getProofAdapter().deserialize(proofValue.asJsonObject());
                        JwsLinkedDataSignature suite2 = new JwsLinkedDataSignature(signatureSuite2);

                        // verify signature
                        System.out.println("VERIFICATION TEST - JWS = " + proof2.getJws());
//                        boolean isValid = suite2.verify(data2, proofValue.asJsonObject(), keyPair.getPublic(), "123", proof2.getJws());
                        boolean isValid = suite2.verify(data2, proofValue.asJsonObject(), pubJwk, proof2.getJws());
                        System.out.println("VERIFICATION TEST - IS VALID = " + isValid);

                        assertTrue(isValid);

//                      if (statusVerifier != null && verifiable.isCredential()) {
//                          statusVerifier.verify(verifiable.asCredential().getCredentialStatus())
//                      }

                    }

                }


            } catch (Exception | DocumentError | KeyGenError | SigningError | VerificationError e){
                if(e instanceof KeyGenError && e.getMessage().contains("secp256k1"))
                    System.err.println("\nCurve secp256k1 (alg. ES256K) is supported on Java 8 (used by Android), but it is NOT supported on Java 17! " +
                            "(see https://connect2id.com/products/nimbus-jose-jwt/jca-algorithm-support#alg-support-table)\n");
                e.printStackTrace();
            }

        }

    }

    @DisplayName("Verifier")
    @ParameterizedTest(name = "{0}")
    @MethodSource({ "verifierManifest" })
    @Order(2)
    void verify(VcTestCase testCase) {
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






    /**
     * Convert json string to jakarta json object
     */
    public static JsonObject stringToJakartaJsonObj(String jsonString) throws JsonException, IllegalStateException {
        StringReader stringReader = new StringReader(jsonString);
        JsonReader jsonReader = Json.createReader(stringReader);
        JsonObject jsonObject = jsonReader.readObject();
        jsonReader.close();
        return jsonObject;
    }

    /**
     * Convert jakarta json object to string
     */
    public static String jakartaJsonObjToString(JsonObject jsonObject) throws JsonException, IllegalStateException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = Json.createWriter(stringWriter);
        jsonWriter.writeObject(jsonObject);
        String jsonString = stringWriter.toString();
        jsonWriter.close();
        return jsonString;
    }

    public static String getSignatureAlgorithmFromJwk(JWK jwk) {
        Object crv = jwk.toJSONObject().get("crv");
        String curveName = ((crv != null) ? crv.toString() : null);
        System.out.println("SIGNING TEST - curve = " + curveName);
        String algorithm = JsonWebSignature2020.getAlgorithm(curveName);
        System.out.println("SIGNING TEST - algorithm = " + algorithm);
        return algorithm;
    }

    public static void failWithJsonLd(JsonLdError e) throws DocumentError {
        if (JsonLdErrorCode.LOADING_DOCUMENT_FAILED == e.getCode()) {
            throw new DocumentError(DocumentError.ErrorType.Invalid, "remote", "document");
        }

        if (JsonLdErrorCode.LOADING_REMOTE_CONTEXT_FAILED == e.getCode()) {
            throw new DocumentError(DocumentError.ErrorType.Invalid, "remote", "context");
        }
    }

}
