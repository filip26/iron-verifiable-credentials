package com.apicatalog.di.sd;

class DeriveTest {

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
