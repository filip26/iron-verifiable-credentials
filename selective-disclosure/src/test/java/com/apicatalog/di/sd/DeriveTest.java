package com.apicatalog.di.sd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.jcs.Jcs;
import com.apicatalog.tree.io.java.NativeComposer;
import com.apicatalog.trust.model.ModelResolver;

class DeriveTest {

    @ParameterizedTest
    @MethodSource({ "resources" })
    void testDerive(String resource) throws Throwable {

        var signed = Resources.getMap(resource + ".signed.json");

        var contexts = ModelResolver.getContexts(signed);

        var cursor = Resources.MODEL.createProofCursor(contexts, signed);

        if (cursor == null) {
            fail();
        }

        if (!cursor.next()) {
            fail("No proof(s)");
        }

        if (!cursor.isAccepted()) {
            fail();
        }

        var proof = cursor.proof();

        if (proof.signature() instanceof SDBaseProofValue signature) {
            var derivedSignature = signature.derive(List.of(
                    "/validFrom",
                    "/validUntil",
                    "/credentialSubject/birthCountry"));

            var isVerified = VerifierTest.PROOF_VERIFIER.verify(derivedSignature.proof());
            assertTrue(isVerified);
            assertFalse(cursor.next());

            var document = new LinkedHashMap<String, Object>(derivedSignature.payload().compacted().get());
            
            var composer = new NativeComposer<Map<String, ? extends Object>>();
            DataIntegrityProof.write(derivedSignature.proof(), composer);
            document.put("proof", composer.compose());
            
            var expected = Resources.getMap(resource + ".derived.json");

            assertEquals(new String(Jcs.canonize(expected)), new String(Jcs.canonize(document)));   

        } else {
            fail();
        }
    }

    static final Stream<String> resources() {
        return Resources
                .stream()
                .filter(name -> name.endsWith(".derived.json"))
                .map(name -> name.substring(0, name.length() - ".derived.json".length()))
                .sorted();
    }

//    static final DocumentReader READER = DocumentReader.with(new ECDSASD2023());
//
//    @Test
//    void testDerive() throws IOException, CryptoSuiteError, DocumentError {
//
//        JsonObject sdoc = fetchResource("tv-01-sdoc.jsonld");
//        JsonObject ddoc = fetchResource("tv-01-ddoc.jsonld");
//
//        VerifiableDocument document = READER.read(sdoc);
//
//        assertNotNull(document);
//
//        Proof proof = document.proofs().iterator().next();
//
//        JsonObject derived = proof
//                .derive(Arrays.asList(
//                        "/credentialSubject/boards/0",
//                        "/credentialSubject/boards/1"));
//
//        assertNotNull(derived);
//
//        if (!JsonLdComparison.equals(ddoc, derived)) {
//            System.out.println("Expected:");
//            System.out.println(IssuerTest.write(ddoc));
//            System.out.println("Actual:");
//            System.out.println(IssuerTest.write(derived));
//            fail("Expected does not match actual.");
//        }
//    }
//
//    @Test
//    void testDeriveEmptySelectors() throws IOException, CryptoSuiteError, DocumentError {
//
//        JsonObject sdoc = fetchResource("tv-01-sdoc.jsonld");
//        JsonObject mdoc = fetchResource("tv-01-mdoc.jsonld");
//
//        VerifiableDocument verifiable = READER.read(sdoc);
//
//        assertNotNull(verifiable);
//
//        Proof proof = verifiable.proofs().iterator().next();
//
//        JsonObject derived = proof.derive(Collections.emptyList());
//
//        assertNotNull(derived);
//
//        if (!JsonLdComparison.equals(mdoc, derived)) {
//            System.out.println("Expected:");
//            System.out.println(IssuerTest.write(mdoc));
//            System.out.println("Actual:");
//            System.out.println(IssuerTest.write(derived));
//            fail("Expected does not match actual.");
//        }
//    }
//
//    @Test
//    void testDeriveNullSelectors() throws IOException, DocumentError, CryptoSuiteError {
//
//        JsonObject sdoc = fetchResource("tv-01-sdoc.jsonld");
//        JsonObject mdoc = fetchResource("tv-01-mdoc.jsonld");
//
//        VerifiableDocument verifiable = READER.read(sdoc);
//
//        assertNotNull(verifiable);
//
//        Proof proof = verifiable.proofs().iterator().next();
//
//        JsonObject derived = proof.derive(null);
//
//        assertNotNull(derived);
//
//        if (!JsonLdComparison.equals(mdoc, derived)) {
//            System.out.println("Expected:");
//            System.out.println(IssuerTest.write(mdoc));
//            System.out.println("Actual:");
//            System.out.println(IssuerTest.write(derived));
//            fail("Expected does not match actual.");
//        }
//    }
//
//    JsonObject fetchResource(String name) throws IOException {
//        try (InputStream is = getClass().getResourceAsStream(name)) {
//            return Json.createReader(is).readObject();
//        }
//    }
}
