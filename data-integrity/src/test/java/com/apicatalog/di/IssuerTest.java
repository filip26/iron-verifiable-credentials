package com.apicatalog.di;

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
import com.apicatalog.crypto.bc.BCEd25519Signer;
import com.apicatalog.crypto.bc.BCMLDSASigner;
import com.apicatalog.crypto.bc.BCSLHDSASigner;
import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.di.proof.Ed25519Signature2020;
import com.apicatalog.di.suite.CryptoSuite;
import com.apicatalog.di.suite.ECDSA2019;
import com.apicatalog.di.suite.EdDSA2022;
import com.apicatalog.di.suite.MLDSA2024;
import com.apicatalog.di.suite.SLHDSA2024;
import com.apicatalog.jcs.Jcs;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multibase.MultibaseDecoder;
import com.apicatalog.multicodec.Multicodec;
import com.apicatalog.multicodec.Multicodec.Tag;
import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.multicodec.codec.KeyCodec;
import com.apicatalog.rdf.canon.RdfCanon;
import com.apicatalog.security.AsymmetricSigner;
import com.apicatalog.tree.io.Tree;
import com.apicatalog.tree.io.jakcson.Jackson2Emitter;
import com.apicatalog.tree.io.java.NativeComposer;
import com.apicatalog.trust.data.MapData;
import com.apicatalog.trust.model.DataModel;
import com.apicatalog.trust.payload.GenericPayload;
import com.apicatalog.trust.proof.Proof;
import com.fasterxml.jackson.core.JsonFactory;

public class IssuerTest {

    static final MultibaseDecoder MULTIBASE = MultibaseDecoder.getInstance();

    static final MulticodecDecoder MULTICODEC = MulticodecDecoder.newInstance(
            KeyCodec.P256_PRIVATE,
            KeyCodec.P384_PRIVATE,
            KeyCodec.ED25519_PRIVATE,
            KeyCodec.MLDSA_44_PRIVATE,
            KeyCodec.SLHDSA_SHA2_128S_PRIVATE,
            // TODO remove when multicodec is updated
            Multicodec.of("falcon-512-pub", Tag.Key, 4652));

    @ParameterizedTest
    @MethodSource({ "resources" })
    void testIssue(String resource) throws Throwable {

        Map<String, String> keys = Resources.getMap(resource + ".keys.json");
        Map<String, Object> options = Resources.getMap(resource + ".options.json");
        Map<String, Object> document = Resources.getMap(resource + ".unsigned.json");

        var privateKey = MULTIBASE.decode(keys.get("secretKeyMultibase"));
        var privateKeyCodec = MULTICODEC.getCodec(privateKey).orElseThrow();

        final String keyAlgorithm;
        final AsymmetricSigner signer;

        switch (privateKeyCodec.code()) {
        case KeyCodec.ED25519_PRIVATE_CODE:
            keyAlgorithm = EdDSA2022.ALGORITHM;
            signer = BCEd25519Signer.newInstance(privateKeyCodec.decode(privateKey))::sign;
            break;
        // Use a secure random number generator to create non-deterministic signatures
        // for the algorithms below in production environments.
        case KeyCodec.P256_PRIVATE_CODE:
            keyAlgorithm = ECDSA2019.P256;
            signer = BCECDSASigner.newP256Instance(privateKeyCodec.decode(privateKey))::sign;
            break;
        case KeyCodec.P384_PRIVATE_CODE:
            keyAlgorithm = ECDSA2019.P384;
            signer = BCECDSASigner.newP384Instance(privateKeyCodec.decode(privateKey))::sign;
            break;
        case KeyCodec.MLDSA_44_PRIVATE_CODE:
            keyAlgorithm = MLDSA2024.ALGORITHM_44;
            signer = BCMLDSASigner.new44Instance(privateKeyCodec.decode(privateKey))::sign;
            break;
        case KeyCodec.SLHDSA_SHA2_128S_PRIVATE_CODE:
            keyAlgorithm = SLHDSA2024.ALGORITHM_SHA2_128s;
            signer = BCSLHDSASigner.new128sInstance(privateKeyCodec.decode(privateKey))::sign;
            break;

        default:
            throw new IllegalArgumentException(
                    """
                    Unsupported secret key algorithm %s (%d).
                    """
                            .formatted(privateKeyCodec.name(), privateKeyCodec.code()));
        }
        ;

        Proof proof = null;

        var proofs = document.remove("proof");

        var composer = new NativeComposer<Map<String, ? extends Object>>();

        if (DataIntegrityProof.TYPE_NAME.equals(options.get("type"))) {

            var proofDraft = DataIntegrityProof.newDraft(
                    options,
                    IssuerTest::getInstance);

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
                        
            proof = proofDraft.generateProof(
                    keyAlgorithm,
                    signer,
                    Resources.DIGEST_FACTORY::get,
                    proofDraft,
                    new GenericPayload(canonicalPayload));

            DataIntegrityProof.write((DataIntegrityProof) proof, composer);

            if (proofDraft.context() != null && !proofDraft.context().isEmpty()) {
                document.put("@context", merge((Collection) document.get("@context"), proofDraft.context()));
            }

        } else if (Ed25519Signature2020.TYPE_NAME.equals(options.get("type"))) {

            assertEquals(Ed25519Signature2020.SIGNATURE_ALGORITHM, keyAlgorithm);

            var proofDraft = Ed25519Signature2020.newDraft((Map) options);

            byte[] canonicalPayload = rdfc(document);

            proof = Ed25519Signature2020.generateProof(
                    signer,
                    Resources.DIGEST_FACTORY::get,
                    proofDraft,
                    new GenericPayload(canonicalPayload));

            Ed25519Signature2020.write((Ed25519Signature2020) proof, composer);

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

    public static CryptoSuite getInstance(String id) {

        return switch (id) {
        case "eddsa-rdfc-2022" -> EdDSA2022.withRDFC();
        case "eddsa-jcs-2022" -> EdDSA2022.withJCS();

        case "ecdsa-rdfc-2019" -> ECDSA2019.withRDFC();
        case "ecdsa-jcs-2019" -> ECDSA2019.withJCS();

        case "mldsa44-rdfc-2024" -> MLDSA2024.get44withRDFC();
        case "mldsa44-jcs-2024" -> MLDSA2024.get44withJCS();

        case "slhdsa128-rdfc-2024" -> SLHDSA2024.get128withRDFC();
        case "slhdsa128-jcs-2024" -> SLHDSA2024.get128withJCS();

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

    public static void main(String[] args) {

        var c = KeyCodec.SLHDSA_SHA2_128S_PRIVATE;
        var d = Multibase.BASE_16.decode(
                "f765d610794caa0dd67472ed92b8ec0b23c1d57c8ed25a9147be7dcd5dca241fb4834a55ff26a17f3947a265bc421093a629d2e863381f8f9f6d64f707cf2e95b");
        IO.println(Multibase.BASE_64_URL.encode(c.encode(d)));
    }

}
