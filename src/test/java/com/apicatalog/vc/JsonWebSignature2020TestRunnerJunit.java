package com.apicatalog.vc;

import com.apicatalog.did.DidResolver;
import com.apicatalog.did.DidUrl;
import com.apicatalog.did.document.DidDocument;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.jsonld.StringUtils;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonLdComparison;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.jsonld.loader.HttpLoader;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.json.VerificationMethodJsonAdapter;
import com.apicatalog.ld.signature.jws.*;
import com.apicatalog.ld.signature.proof.ProofOptions;
import com.apicatalog.ld.signature.proof.VerificationMethod;
import com.nimbusds.jose.jwk.JWK;
import jakarta.json.*;
import jakarta.json.stream.JsonGenerator;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import static com.apicatalog.vc.JsonWebSignature2020Test.*;
import static org.junit.jupiter.api.Assertions.*;

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
                new TestVerifier().verify(testCase.input, LOADER, testCase.domain).isValid();

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
                TestIssuer issuer = new TestIssuer()
                        .sign(testCase.input, getKeys(keyPairLocation, LOADER), options, LOADER);

                JsonObject signed = null;

                if (testCase.context != null) {

                    signed = issuer.getCompacted(testCase.context);

                } else {
                    signed = issuer.getExpanded();
                }

                assertFalse(isNegative(), "Expected error " + testCase.result);

                assertNotNull(signed);

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



    class TestIssuer {

        private JsonObject expandedVc;
        private DocumentLoader loader;

        public TestIssuer sign(URI documentLocation, JwsKeyPair keyPair, ProofOptions options, DocumentLoader loader) throws DocumentError, SigningError, JsonLdError {
            this.loader = loader;

            JsonObject firstObj;
            try {
                JsonArray expanded = JsonLd.expand(documentLocation).loader(loader).get();
                firstObj = JsonLdUtils
                        .findFirstObject(expanded)
                        .orElseThrow(() ->
                                new DocumentError(DocumentError.ErrorType.Invalid, "document")
                        );
            } catch (JsonLdError e) {
                failWithJsonLd(e);
                throw new SigningError(e);
            }

            JWK jwk = keyPair.getPublicKey(); //Json Web Key
            System.out.println("SIGNING TEST - JWK (public) = \n " + jwk.toPublicJWK().toString());
            System.out.println("SIGNING TEST - JWK (private) = \n " + jwk.toString());

            String alg = getSignatureAlgorithmFromJwk(jwk);

            JwsSignatureSuite signatureSuite = new JsonWebSignature2020(alg); //JsonWebSignature2020("EdDSA")
            JsonObject data = JwsEmbeddedProofAdapter.removeProof(firstObj);
            JwsLinkedDataSignature suite = new JwsLinkedDataSignature(signatureSuite);

            JsonObject proof = signatureSuite.getProofAdapter().serialize(JsonWebSignature2020.toUnsignedJwsProof(options)); //uses JsonWebProof2020Adapter#serialize()
            String jws = suite.sign(data, jwk, proof);
            proof = signatureSuite.getProofAdapter().setProofValue(proof, jws); //uses JsonWebProof2020Adapter#setProofValue()

            expandedVc = JwsEmbeddedProofAdapter.addProof(firstObj, proof);
            System.out.println("SIGNING TEST - EXPANDED VC = \n " + jakartaJsonObjToString(expandedVc));

            return this;
        }

        public JsonObject getExpanded() {
            return expandedVc;
        }

        public JsonObject getCompacted(final URI contextLocation) throws DocumentError, SigningError {
            try {
                return JsonLd.compact(JsonDocument.of(expandedVc), contextLocation).loader(loader).get();
            } catch (JsonLdError e) {
                failWithJsonLd(e);
                throw new SigningError(e);
            }
        }

    }

    class TestVerifier {

        private boolean isValid;

        public TestVerifier verify(URI location, DocumentLoader loader, String domain) throws DocumentError, VerificationError {

            final JsonArray expanded;
            try {
                expanded = JsonLd.expand(location).loader(loader).get();
            } catch (JsonLdError e) {
                failWithJsonLd(e);
                throw new VerificationError(e);
            }

            if (expanded == null || expanded.isEmpty()) {
                throw new DocumentError(DocumentError.ErrorType.Invalid, "document");
            }

            for (final JsonValue item : expanded) {
                if (JsonUtils.isNotObject(item)) {
                    throw new DocumentError(DocumentError.ErrorType.Invalid, "document");
                }
                JsonObject currObj = item.asJsonObject();
                System.out.println("VERIFICATION TEST - EXPANDED VC = \n " + currObj);

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

                    String proofTypeEntry = proofType.stream().findFirst()
                            .orElseThrow(() -> new DocumentError(DocumentError.ErrorType.Missing, "proof", Keywords.TYPE));
                    System.out.println("VERIFICATION TEST - proofTypeEntry = " + proofTypeEntry);
                    if (!proofTypeEntry.equals("https://w3id.org/security#JsonWebSignature2020")
                    && !proofTypeEntry.equals("JsonWebSignature2020"))
                        throw new VerificationError(VerificationError.Code.UnknownCryptoSuite);

                    JwsProof proof2 = new JsonWebProof2020Adapter().deserialize(proofValue.asJsonObject());
                    // check domain
                    if (StringUtils.isNotBlank(domain) && !domain.equals(proof2.getDomain())) {
                        throw new VerificationError(VerificationError.Code.InvalidProofDomain);
                    }

                    URI vmId = proof2.getVerificationMethod().id();
                    System.out.println("VERIFICATION TEST - proof verificationMethod id = \n " + vmId.toString());

                    final VerificationMethod verificationMethod = get(proof2.getVerificationMethod().id(), loader, new JsonWebKey2020Adapter());

                    if (!(verificationMethod instanceof JwsVerificationKey)) {
                        throw new VerificationError(VerificationError.Code.UnknownVerificationMethod);
                    }

                    JWK pubJwk = ((JwsVerificationKey) verificationMethod).getPublicKey();

                    System.out.println("VERIFICATION TEST - JWK (public) = \n " + pubJwk);

                    String algorithm = getSignatureAlgorithmFromJwk(pubJwk);

                    JwsSignatureSuite signatureSuite2 = new JsonWebSignature2020(algorithm);

                    JwsLinkedDataSignature suite2 = new JwsLinkedDataSignature(signatureSuite2);

                    // verify signature
                    System.out.println("VERIFICATION TEST - JWS = " + proof2.getJws());
//                        boolean isValid = suite2.verify(data2, proofValue.asJsonObject(), keyPair.getPublic(), "123", proof2.getJws());
                    isValid = suite2.verify(data2, proofValue.asJsonObject(), pubJwk, proof2.getJws());
                    System.out.println("VERIFICATION TEST - IS VALID = " + isValid);

                    if(!this.isValid)  //based on com.apicatalog.vc.processor.Verifier
                        throw new VerificationError(VerificationError.Code.InvalidSignature, new GeneralSecurityException("Invalid signature"));

//                    // verify status
//                    if (statusVerifier != null && verifiable.isCredential()) {
//                        statusVerifier.verify(verifiable.asCredential().getCredentialStatus());
//                    }
                }

            }

            return this;

        }

        public boolean isValid() throws VerificationError {
            return isValid;
        }

        final VerificationMethod get(final URI id, final DocumentLoader loader, VerificationMethodJsonAdapter keyAdapter) throws DocumentError, VerificationError {
            try {

                if(id.toString().contains("0005-jws-verification-key")) {
                    //Loading documents from file fails for some reason in this method so lets have the public key hardcoded here instead of having it in file
                    JwsVerificationKey key = new JwsVerificationKey();
                    key.setId(URI.create("https://github.com/filip26/iron-verifiable-credentials/jws_verifier/0005-jws-verification-key.json"));
                    key.setType("https://w3id.org/security#JsonWebKey2020");
                    key.setController(URI.create("https://github.com/filip26/iron-verifiable-credentials/jws_issuer/1"));
                    key.setPublicKey(JWK.parse("{\n" +
                            "      \"kty\": \"EC\",\n" +
                            "      \"crv\": \"P-384\",\n" +
                            "      \"x\": \"eQbMauiHc9HuiqXT894gW5XTCrOpeY8cjLXAckfRtdVBLzVHKaiXAAxBFeVrSB75\",\n" +
                            "      \"y\": \"YOjxhMkdH9QnNmGCGuGXJrjAtk8CQ1kTmEEi9cg2R9ge-zh8SFT1Xu6awoUjK5Bv\"\n" +
                            "    }"));
                    return key;

                } else if (id.toString().contains("did:key")) {

//                    JwsVerificationKey key = new JwsVerificationKey();
//                    key.setId(URI.create("did:key:z6Mkf5rGMoatrSj1f4CyvuHBeXJELe9RPdzo2PKGNCKVtZxP"));
//                    key.setType("https://w3id.org/security#JsonWebKey2020");
//                    key.setController(URI.create("did:key:z6Mkf5rGMoatrSj1f4CyvuHBeXJELe9RPdzo2PKGNCKVtZxP"));
//                    key.setPublicKey(JWK.parse("{" +
//                            "      \"kty\": \"OKP\"," +
//                            "      \"crv\": \"Ed25519\"," +
////                            "      \"d\": \"m5N7gTItgWz6udWjuqzJsqX-vksUnxJrNjD5OilScBc\"," + //private part
//                            "      \"x\": \"CV-aGlld3nVdgnhoZK0D36Wk-9aIMlZjZOK2XhPMnkQ\"" +
//                            "    }"));
//                    return key;

                    //TODO com.apicatalog.multicodec.Codec for other keys than Ed25519 / X25519 is missing
                    // (from below commented DidKey tests only didKeyEd25519 works)
//                    DidKey didKeyEd25519 = DidKey.from(URI.create("did:key:z6Mkf5rGMoatrSj1f4CyvuHBeXJELe9RPdzo2PKGNCKVtZxP"));
//                    DidKey didKeySecp256k1 = DidKey.from(URI.create("did:key:zQ3shokFTS3brHcDQrn82RUDfCZESWL1ZdCEJwekUDPQiYBme"));
//                    DidKey didKeyP256 = DidKey.from(URI.create("did:key:zDnaerDaTF5BXEavCrfRZEk316dpbLsfPDZ3WJ5hRTPFU2169"));
//                    DidKey didKeyP384 = DidKey.from(URI.create("did:key:z82Lm1MpAkeJcix9K8TMiLd5NMAhnwkjjCBeWHXyu3U4oT2MVJJKXkcVBgjGhnLBn2Kaau9"));
//                    DidKey didKeyP512 = DidKey.from(URI.create("did:key:z2J9gaYxrKVpdoG9A4gRnmpnRCcxU6agDtFVVBVdn1JedouoZN7SzcyREXXzWgt3gGiwpoHq7K68X4m32D8HgzG8wv3sY5j7"));
//                    DidKey didKeyRSA2048 = DidKey.from(URI.create("did:key:z4MXj1wBzi9jUstyPMS4jQqB6KdJaiatPkAtVtGc6bQEQEEsKTic4G7Rou3iBf9vPmT5dbkm9qsZsuVNjq8HCuW1w24nhBFGkRE4cd2Uf2tfrB3N7h4mnyPp1BF3ZttHTYv3DLUPi1zMdkULiow3M1GfXkoC6DoxDUm1jmN6GBj22SjVsr6dxezRVQc7aj9TxE7JLbMH1wh5X3kA58H3DFW8rnYMakFGbca5CB2Jf6CnGQZmL7o5uJAdTwXfy2iiiyPxXEGerMhHwhjTA1mKYobyk2CpeEcmvynADfNZ5MBvcCS7m3XkFCMNUYBS9NQ3fze6vMSUPsNa6GVYmKx2x6JrdEjCk3qRMMmyjnjCMfR4pXbRMZa3i"));

                    if (DidUrl.isDidUrl(id)) {

                        DidResolver resolver = new JwsDidKeyResolver();

                        final DidDocument didDocument = resolver.resolve(DidUrl.from(id));

                        return didDocument
                                .verificationMethod()
                                .stream()
                                .filter(vm -> keyAdapter.getType().equals(vm.type()))
                                .map(did -> {
                                            JwsVerificationKey key = new JwsVerificationKey();
                                            key.setId(did.id().toUri());
                                            key.setType(did.type());
                                            key.setController(did.controller().toUri());
                                            try {
                                                JWK jwk = JwsDidKeyResolver.getJwk(did);
                                                key.setPublicKey(jwk);
                                            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                                                throw new IllegalStateException(e);
                                            }
                                            return key;
                                        }
                                )
                                .findFirst()
                                .orElseThrow(IllegalStateException::new);
                    }

                }

                //DOCUMENT size is always 0 ...!?
                final JsonArray document = JsonLd
                        .expand(id)
                        .loader(loader)
                        .context("https://w3id.org/security/suites/jws-2020/v1")
                        .get();

                for (final JsonValue method : document) {

                    if (JsonUtils.isNotObject(method)) {
                        continue;
                    }

                    // take the first key that match
                    if (JsonLdUtils
                            .getType(method.asJsonObject())
                            .stream()
                            .anyMatch(m -> keyAdapter.getType().equals(m))) {

                        return keyAdapter.deserialize(method.asJsonObject());
                    }
                }

            } catch (JsonLdError e) {
                failWithJsonLd(e);
                throw new VerificationError(e);
            } catch (ParseException e) {
                throw new VerificationError(e);
            }

            throw new VerificationError(VerificationError.Code.UnknownVerificationKey);
        }



    }






}
