package com.apicatalog.di.sd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.apicatalog.crypto.bc.BCECDSASigner;
import com.apicatalog.crypto.bc.BCMLDSASigner;
import com.apicatalog.crypto.bc.BCSLHDSASigner;
import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.di.suite.CryptoSuite;
import com.apicatalog.di.suite.ECDSA2019;
import com.apicatalog.di.suite.ECDSASD2023;
import com.apicatalog.di.suite.MLDSA2024;
import com.apicatalog.di.suite.SLHDSA2024;
import com.apicatalog.jcs.Jcs;
import com.apicatalog.multicodec.codec.KeyCodec;
import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.tree.io.java.NativeComposer;

public class IssuerTest {

    @ParameterizedTest
    @MethodSource({ "resources" })
    void testIssue(String resource) throws Throwable {

        Map<String, Object> keysMap = Resources.getMap(resource + ".keys.json");
        Map<String, Object> options = Resources.getMap(resource + ".options.json");
        Map<String, Object> document = Resources.getMap(resource + ".unsigned.json");

        var keys = Keys.from(keysMap);

        final String keyAlgorithm;
        final AsymmetricSigner baseSigner;
        final AsymmetricSigner proofSigner;

        switch (keys.codec().code()) {
        // Use a secure random number generator to create non-deterministic signatures
        // for the algorithms below in production environments.
        case KeyCodec.P256_PRIVATE_CODE:
            keyAlgorithm = ECDSA2019.P256;
            baseSigner = BCECDSASigner.newP256Instance(keys.baseSecretKey())::sign;
            proofSigner = BCECDSASigner.newP256Instance(keys.proofSecretKey())::sign;
            break;
        case KeyCodec.P384_PRIVATE_CODE:
            keyAlgorithm = ECDSA2019.P384;
            baseSigner = BCECDSASigner.newP384Instance(keys.baseSecretKey())::sign;
            proofSigner = BCECDSASigner.newP384Instance(keys.proofSecretKey())::sign;
            break;
        case KeyCodec.MLDSA_44_PRIVATE_CODE:
            keyAlgorithm = MLDSA2024.ALGORITHM_44;
            baseSigner = BCMLDSASigner.new44Instance(keys.baseSecretKey())::sign;
            proofSigner = BCMLDSASigner.new44Instance(keys.proofSecretKey())::sign;
            break;
        case KeyCodec.SLHDSA_SHA2_128S_PRIVATE_CODE:
            keyAlgorithm = SLHDSA2024.ALGORITHM_SHA2_128s;
            baseSigner = BCSLHDSASigner.new128sInstance(keys.baseSecretKey())::sign;
            proofSigner = BCSLHDSASigner.new128sInstance(keys.proofSecretKey())::sign;
            break;
        default:
            throw new IllegalArgumentException(
                    """
                    Unsupported secret key algorithm %s (%d).
                    """
                            .formatted(keys.codec().name(), keys.codec().code()));
        }

        DataIntegrityProof proof = null;
        var proofs = document.get("proof");

        var composer = new NativeComposer<Map<String, ? extends Object>>();

        if (DataIntegrityProof.TYPE_NAME.equals(options.get("type"))) {

            var proofDraft = DataIntegrityProof.newBuilder(
                    options,
                    IssuerTest::getCryptoSuite);

            var processor = Resources.MODEL.createProcessor(document);

            processor.withProofs(proofDraft.previous());
            
            if (proofDraft.cryptosuite() instanceof ECDSASD2023 suite) {

                proof = suite.sign(
                        keyAlgorithm,
                        baseSigner,
                        keys.proofPublicKey(),
                        proofSigner,
                        Resources.DIGEST_FACTORY::get,
                        proofDraft,
                        processor.redactable(
                                (Collection<String>) options.get("mandatoryPointers"), // TODO separate it from draft
                                Map.of("HMAC_KEY", keys.hmacKey())));

            } else {
                fail();
            }

            DataIntegrityProof.write(proof, composer);

            if (proofDraft.context() != null && !proofDraft.context().isEmpty()) {
                document.put("@context", merge((Collection) document.get("@context"), proofDraft.context()));
            }

        } else {
            fail("An unsupported proof type " + options.get("type"));
        }

        var proofMap = composer.compose();

        if (proofs instanceof Collection col) {
            var clone = new ArrayList<>(col);
            col.add(proofMap);
            proofs = col;

        } else if (proofs == null) {
            proofs = proofMap;

        } else {
            var col = new ArrayList<>();
            col.add(proofs);
            col.add(proofMap);
            proofs = col;
        }

        document.put("proof", proofs);

        var expected = Resources.getMap(resource + ".signed.json");

        assertEquals(new String(Jcs.canonize(expected)), new String(Jcs.canonize(document)));
    }

    public static CryptoSuite getCryptoSuite(String id) {

        return switch (id) {
        case "ecdsa-sd-2023" -> ECDSASD2023.getInstance();

        default -> throw new IllegalArgumentException();
        };
    }

    static final Stream<String> resources() throws IOException {
        return Resources.stream()
                .filter(name -> name.endsWith("unsigned.json"))
                .map(name -> name.substring(0, name.indexOf('.')))
                .sorted();
    }

    static Collection<String> merge(Collection<String> documentContext, Collection<String> proofContext) {

        var result = new LinkedHashSet<>(documentContext);

        result.addAll(proofContext);

        return result;
    }

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
