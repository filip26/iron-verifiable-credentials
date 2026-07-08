package com.apicatalog.di.sd;

public class VerifierTest {

//    public final static DocumentLoader LOADER = new StaticContextLoader(new SchemeRouter());
////    public final static DocumentLoader LOADER = new StaticContextLoader(
////            new UriBaseRewriter(
////                    VcTestCase.BASE,
////                    "classpath:",
////                    new SchemeRouter().set("classpath", new ClasspathLoader())));
//
//    static final Verifier VERIFIER = Verifier.with(new ECDSASD2023())
//            .methodResolver(defaultResolvers(LOADER));
//
//    @Test
//    void testVerifyBase() throws IOException, VerificationError, DocumentError {
//        JsonObject sdoc = fetchResource("tv-01-sdoc.jsonld");
//        assertThrows(VerificationError.class, () -> VERIFIER.verify(sdoc));
//    }
//
//    @Test
//    void testVerifyDerived() throws IOException, VerificationError, DocumentError {
//
//        JsonObject ddoc = fetchResource("tv-01-ddoc.jsonld");
//
//        VerifiableDocument verifiable = VERIFIER.verify(ddoc);
//
//        assertNotNull(verifiable);
//    }
//
//    @Test
//    void testVerifyDerivedMandatory() throws IOException, VerificationError, DocumentError {
//
//        JsonObject ddoc = fetchResource("tv-01-mdoc.jsonld");
//
//        VerifiableDocument verifiable = VERIFIER.verify(ddoc);
//
//        assertNotNull(verifiable);
//    }
//
//    JsonObject fetchResource(String name) throws IOException {
//        try (InputStream is = getClass().getResourceAsStream(name)) {
//            return Json.createReader(is).readObject();
//        }
//    }
//
//    public static final VerificationKeyProvider defaultResolvers(DocumentLoader loader) {
//        return MethodSelector.create()
//                // accept did:key
//                .with(MethodPredicate.methodId(DidKey::isDidKeyUrl),
//                        ControllableKeyProvider.of(new DidKeyResolver(ECDSASD2023.CODECS)))
//                .build();
//    }
}
