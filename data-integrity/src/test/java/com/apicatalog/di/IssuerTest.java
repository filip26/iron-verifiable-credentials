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
import com.apicatalog.di.suite.CryptoSuites;
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
import com.apicatalog.trust.data.GenericPayload;
import com.apicatalog.trust.data.MapData;
import com.apicatalog.trust.proof.Proof;
import com.fasterxml.jackson.core.JsonFactory;

public class IssuerTest {

    static final MultibaseDecoder MULTIBASE = MultibaseDecoder.getInstance();

    static final MulticodecDecoder MULTICODEC = MulticodecDecoder.getInstance(
            KeyCodec.P256_PUBLIC_KEY,
            KeyCodec.P256_PRIVATE_KEY,
            KeyCodec.P384_PUBLIC_KEY,
            KeyCodec.P384_PRIVATE_KEY,
            KeyCodec.ED25519_PUBLIC_KEY,
            KeyCodec.ED25519_PRIVATE_KEY,
            KeyCodec.MLDSA_44_PUBLIC_KEY,
            KeyCodec.MLDSA_44_PRIVATE_KEY,
            Multicodec.of("slhdsa-sha2-128-priv", Tag.Key, 464000000), // FIXME temporary, there is not private key code
                                                                       // yet
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

        switch (privateKeyCodec.name()) {
        case "ed25519-priv":
            keyAlgorithm = "Ed25519";
            signer = BCEd25519Signer.newInstance(privateKeyCodec.decode(privateKey))::sign;
            break;
        // Use a secure random number generator to create non-deterministic signatures
        // for the algorithms below in production environments.
        case "p256-priv":
            keyAlgorithm = "P-256";
            signer = BCECDSASigner.newP256Instance(privateKeyCodec.decode(privateKey))::sign;
            break;
        case "p384-priv":
            keyAlgorithm = "P-384";
            signer = BCECDSASigner.newP384Instance(privateKeyCodec.decode(privateKey))::sign;
            break;
        case "mldsa-44-priv":
            keyAlgorithm = "ML-DSA-44";
            signer = BCMLDSASigner.newInstance(privateKeyCodec.decode(privateKey))::sign;
            break;
        case "slhdsa-sha2-128-priv":
            keyAlgorithm = "SLH-DSA-SHA2-128s";
            signer = BCSLHDSASigner.new128SInstance(privateKeyCodec.decode(privateKey))::sign;
            break;

        default:
            throw new IllegalArgumentException(
                    """
                    Unsupported secret key algorithm %s.
                    """
                            .formatted(privateKeyCodec.name()));
        }
        ;

        Proof proof = null;

        var proofs = document.remove("proof");

        var composer = new NativeComposer<Map<String, ? extends Object>>();

        if (DataIntegrityProof.TYPE_NAME.equals(options.get("type"))) {

            var proofDraft = DataIntegrityProof.newDraft(
                    options,
                    cryptosuite -> CryptoSuites.getInstance(cryptosuite, keyAlgorithm));

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
            case "JCS" -> Jcs.canonize(c14nData);
            case "RDFC" -> rdfc(c14nData);
            default -> throw new IllegalStateException(
                    """
                    Unsupported c14n = %s.
                    """.formatted(proofDraft.cryptosuite().c14n()));
            };

            var data = new MapData(document, proofDraft.c14n());
            data.digestiblePayload(proofDraft.previous(), new GenericPayload(canonicalPayload));

            proof = proofDraft.generateProof(
                    signer,
                    proofDraft,
                    data);

            DataIntegrityProof.write((DataIntegrityProof) proof, composer);

            if (proofDraft.context() != null && !proofDraft.context().isEmpty()) {
                document.put("@context", merge((Collection) document.get("@context"), proofDraft.context()));
            }

//            IO.println("P: " + Multibase.BASE_58_BTC.encode(proof.signature().toByteArray()));

        } else if (Ed25519Signature2020.TYPE_NAME.equals(options.get("type"))) {

            assertEquals(Ed25519Signature2020.KEY_ALGORITHM, keyAlgorithm);

            var proofDraft = Ed25519Signature2020.newDraft((Map) options);

            byte[] canonicalPayload = rdfc(document);

            var data = new MapData(document, Ed25519Signature2020.C14N);
            data.digestiblePayload(new GenericPayload(canonicalPayload));

            proof = Ed25519Signature2020.generateProof(
                    signer,
                    proofDraft,
                    data);

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

        var canon = RdfCanon.create("SHA-256");
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

        var c = Multicodec.of("slhdsa-sha2-128-priv", Tag.Key, 464000000); // FIXME temporary, there is not private key
                                                                           // code yet
        var d = Multibase.BASE_16.decode(
                "f765d610794caa0dd67472ed92b8ec0b23c1d57c8ed25a9147be7dcd5dca241fb4834a55ff26a17f3947a265bc421093a629d2e863381f8f9f6d64f707cf2e95b");
        IO.println(Multibase.BASE_64_URL_PAD.encode(c.encode(d)));
    }

}
