package com.apicatalog.di.sd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
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
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.multicodec.codec.KeyCodec;
import com.apicatalog.rdf.canon.RdfCanon;
import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.tree.io.Tree;
import com.apicatalog.tree.io.jakcson.Jackson2Emitter;
import com.apicatalog.tree.io.java.NativeComposer;
import com.apicatalog.trust.model.DataModel;
import com.apicatalog.trust.payload.GenericPayload;
import com.fasterxml.jackson.core.JsonFactory;

public class IssuerTest {

    @ParameterizedTest
    @MethodSource({ "resources" })
    void testIssue(String resource) throws Throwable {

        Map<String, Object> keysMap = Resources.getMap(resource + ".keys.json");
        Map<String, Object> options = Resources.getMap(resource + ".options.json");
        Map<String, Object> document = Resources.getMap(resource + ".unsigned.json");

        var keys = Keys.from(keysMap);

        final String keyAlgorithm;
        final AsymmetricSigner signer;

        switch (keys.codec().code()) {
        // Use a secure random number generator to create non-deterministic signatures
        // for the algorithms below in production environments.
        case KeyCodec.P256_PRIVATE_CODE:
            keyAlgorithm = ECDSA2019.P256;
            signer = BCECDSASigner.newP256Instance(keys.baseSecretKey())::sign;
            break;
        case KeyCodec.P384_PRIVATE_CODE:
            keyAlgorithm = ECDSA2019.P384;
            signer = BCECDSASigner.newP384Instance(keys.baseSecretKey())::sign;
            break;
        case KeyCodec.MLDSA_44_PRIVATE_CODE:
            keyAlgorithm = MLDSA2024.ALGORITHM_44;
            signer = BCMLDSASigner.new44Instance(keys.baseSecretKey())::sign;
            break;
        case KeyCodec.SLHDSA_SHA2_128S_PRIVATE_CODE:
            keyAlgorithm = SLHDSA2024.ALGORITHM_SHA2_128s;
            signer = BCSLHDSASigner.new128sInstance(keys.baseSecretKey())::sign;
            break;
        default:
            throw new IllegalArgumentException(
                    """
                    Unsupported secret key algorithm %s (%d).
                    """
                            .formatted(keys.codec().name(), keys.codec().code()));
        }

        DataIntegrityProof proof = null;

        var proofs = document.remove("proof");

        var composer = new NativeComposer<Map<String, ? extends Object>>();

        if (DataIntegrityProof.TYPE_NAME.equals(options.get("type"))) {

            var proofDraft = DataIntegrityProof.newBuilder(
                    options,
                    IssuerTest::getCryptoSuite);

            var c14nData = document;

            if (proofDraft.previous() != null && !proofDraft.previous().isEmpty()) {
                // TODO better, use model
                var previousProofs = new ArrayList<>(proofDraft.previous().size());
                for (var p : (Collection<Map<String, Object>>) proofs) {
                    if (proofDraft.previous().contains(p.get("id"))) {
                        previousProofs.add(p);
                    }
                }

                c14nData = new LinkedHashMap<String, Object>(document);
                c14nData.put("proof", previousProofs);
            }

            var canonicalPayload = switch (proofDraft.c14n()) {
            case DataModel.C14N_JCS -> Jcs.canonize(c14nData);
            case DataModel.C14N_RDFC -> rdfc(c14nData);
            default -> throw new IllegalStateException(
                    """
                    Unsupported c14n = %s.
                    """.formatted(proofDraft.cryptosuite().c14n()));
            };

//            payload.withProofs(proof.previous());

            proof = proofDraft.sign(
                    keyAlgorithm,
                    signer,
                    Resources.DIGEST_FACTORY::get,
                    proofDraft,
                    new GenericPayload(canonicalPayload));

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

    static final byte[] rdfc(Map<String, ?> document) throws IOException, JsonLdError {

        // TODO temporary, remove with Titanium v2.x.x
        var bos = new ByteArrayOutputStream();
        try (var emitter = Jackson2Emitter.newEmitter(bos, JsonFactory.builder().build())) {
            Tree.write(document, emitter);
        }

        var toRdf = JsonLd.toRdf(JsonDocument.of(new ByteArrayInputStream(bos.toByteArray())))
                .loader(ContextLoader.getInstance());

        var canon = RdfCanon.create(Resources.DIGEST_FACTORY.get("SHA-256"));
        toRdf.provide(canon);

        bos.reset();

        canon.provide(s -> {
            try {
                bos.write(s.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
//        System.out.println(new String(bos.toByteArray()));
        return bos.toByteArray();
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
