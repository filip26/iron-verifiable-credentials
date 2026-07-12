package com.apicatalog.di.sd;

public class IssuerTest {

//    final static ECDSASD2023 SUITE = new ECDSASD2023();
//    
//    final static byte[] HMACK_KEY = Hex.decode("00112233445566778899AABBCCDDEEFF00112233445566778899AABBCCDDEEFF");
//
//    final static Multikey KEYS = GenericMultikey.of(null, null, null,
//            GenericMulticodecKey.of(
//                    KeyCodec.P256_PRIVATE_KEY,
//                    Multibase.BASE_58_BTC,
//                    "z42twTcNeSYcnqg1FLuSFs2bsGH3ZqbRHFmvS9XMsYhjxvHN"));
//
//    final static Multikey PROOF_KEYS = GenericMultikey.of(null, null,
//            GenericMulticodecKey.of(
//                    KeyCodec.P256_PUBLIC_KEY,
//                    Multibase.BASE_58_BTC,
//                    "zDnaeTHfhmSaQKBc7CmdL3K7oYg3D6SC7yowe2eBeVd2DH32r"),
//            GenericMulticodecKey.of(
//                    KeyCodec.P256_PRIVATE_KEY,
//                    Multibase.BASE_58_BTC,
//                    "z42tqvNGyzyXRzotAYn43UhcFtzDUVdxJ7461fwrfhBPLmfY"));
//
//    final static ECDSASD2023Issuer ISSUER = SUITE.createIssuer(KEYS);
//    
//    static {
//        ISSUER.loader(VerifierTest.LOADER);
//    }
//
//    
//    final static Collection<String> MP_TV = Arrays.asList(
//            "/issuer",
//            "/credentialSubject/sailNumber",
//            "/credentialSubject/sails/1",
//            "/credentialSubject/boards/0/year",
//            "/credentialSubject/sails/2");
//
//    @Test
//    void testSign() throws IOException, CryptoSuiteError, JsonLdError, CryptoSuiteError, DocumentError {
//
//        JsonObject udoc = fetchResource("tv-01-udoc.jsonld");
//        JsonObject sdoc = fetchResource("tv-01-sdoc.jsonld");
//        
//        ECDSASD2023Draft draft = ISSUER.createDraft(URI.create("did:key:zDnaepBuvsQ8cpsWrVKw8fbpGpvPeNSjVPTWoq6cRqaYzBKVP#zDnaepBuvsQ8cpsWrVKw8fbpGpvPeNSjVPTWoq6cRqaYzBKVP"));
//        
//        draft.purpose(URI.create(VcdmVocab.SECURITY_VOCAB + "assertionMethod"));        
//        draft.created(Instant.parse("2023-08-15T23:36:38Z"));
//        draft.selectors(MP_TV);
//        draft.proofKeys(PROOF_KEYS);
//        draft.hmacKey(HMACK_KEY);
//
//        JsonObject signed = ISSUER.sign(udoc, draft);
//
//        assertNotNull(signed);
//
//        if (!JsonLdComparison.equals(sdoc, signed)) {
//            System.out.println("Expected:");
//            System.out.println(write(sdoc));
//            System.out.println("Actual:");
//            System.out.println(write(signed));
//            fail("Expected does not match actual.");
//        }
//    }
//
//    @Test
//    void testSignGeneratedKeys() throws IOException, CryptoSuiteError, JsonLdError, CryptoSuiteError, DocumentError, CryptoSuiteError, VerificationError {
//
//        JsonObject udoc = fetchResource("tv-01-udoc.jsonld");
//        ECDSASD2023Draft draft = ISSUER.createDraft(URI.create("did:key:zDnaepBuvsQ8cpsWrVKw8fbpGpvPeNSjVPTWoq6cRqaYzBKVP#zDnaepBuvsQ8cpsWrVKw8fbpGpvPeNSjVPTWoq6cRqaYzBKVP"));
//        
//        draft.purpose(URI.create(VcdmVocab.SECURITY_VOCAB + "assertionMethod"));        
//   
//        draft.created(Instant.parse("2023-08-15T23:36:38Z"));
//        draft.selectors(MP_TV);
//        draft.useGeneratedHmacKey(32);
//        draft.useGeneratedProofKeys();
//
//        JsonObject signed = ISSUER.sign(udoc, draft);
//
//        assertNotNull(signed);
//    }
//
//    @Test
//    void testSignEmptyMandatoryPointers() throws IOException, CryptoSuiteError, JsonLdError, CryptoSuiteError, DocumentError {
//
//        JsonObject udoc = fetchResource("tv-01-udoc.jsonld");
//        
//        ECDSASD2023Draft draft = ISSUER.createDraft(URI.create("did:key:zDnaepBuvsQ8cpsWrVKw8fbpGpvPeNSjVPTWoq6cRqaYzBKVP#zDnaepBuvsQ8cpsWrVKw8fbpGpvPeNSjVPTWoq6cRqaYzBKVP"));
//        
//        draft.purpose(URI.create(VcdmVocab.SECURITY_VOCAB + "assertionMethod"));        
//        draft.created(Instant.parse("2023-08-15T23:36:38Z"));
//        draft.selectors(Collections.emptySet());
//        draft.proofKeys(PROOF_KEYS);
//        draft.hmacKey(HMACK_KEY);
//
//        JsonObject signed = ISSUER.sign(udoc, draft);
//
//        assertNotNull(signed);
//    }
//    JsonObject fetchResource(String name) throws IOException {
//        try (InputStream is = getClass().getResourceAsStream(name)) {
//            return Json.createReader(is).readObject();
//        }
//    }
//
//    public static String write(JsonValue doc) {
//        StringWriter sw = new StringWriter();
//        final JsonWriterFactory writerFactory = Json.createWriterFactory(
//                Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true));
//
//        try (JsonWriter writer = writerFactory.createWriter(sw)) {
//            writer.write(doc);
//        }
//        return sw.toString();
//    }
}
