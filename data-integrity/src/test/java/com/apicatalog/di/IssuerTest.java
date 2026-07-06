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

import com.apicatalog.crypto.bc.BcEcdsaSigner;
import com.apicatalog.crypto.bc.BcEd25519Signer;
import com.apicatalog.crypto.bc.BcMlDsaSigner;
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
            signer = BcEd25519Signer.getInstance(privateKeyCodec.decode(privateKey))::sign;
            break;
        case "p256-priv":
            keyAlgorithm = "P-256";
            signer = BcEcdsaSigner.getP256Instance(privateKeyCodec.decode(privateKey))::sign;
            break;
        case "p384-priv":
            keyAlgorithm = "P-384";
            signer = BcEcdsaSigner.getP384Instance(privateKeyCodec.decode(privateKey))::sign;
            break;
        case "mldsa-44-priv":
            keyAlgorithm = "ML-DSA-44";
            signer = BcMlDsaSigner.getInstance(privateKeyCodec.decode(privateKey))::sign;
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
        var d = Multibase.BASE_16.decode(
                "f4a0ed63c0e8fdd5f3606cae5fb058fb574360ce05b928b0d2015d3be7895c51f2e39354028474102fa86be058cd02940e7b26be707a2e7c13aca80b2bbd8b8d1d07b6766ddf790564d3b98ad67640fda16e2a327e981afe0673a7f5a10919fcd25304bdfa13df88ed552e32896e159db019acb23590d2cca0fae597bffe1b36614010053a69083008d0b4249e28829d8c68552800804296d64b485409445484031e24492534892643272d1402d9b067104b070a2b82521b1511b998c0c088a0b482d9b266a62a27004b00c24482e0834210413001a2329e3842904350464126ddca66120b2080ab94c1b394a420480c22489c3b851220990d1c630202924092286c8408a9984000b8145da06290ab145908828e10884caa06c9a460803b4255b46828304851a4501223410d414298836324cc400a2964803c30990400c0c39069c869094182210c54c608841113241020145cca8848a024918a200e3824d4236660a066e23424402a6655b308019b62901166a2031091ba401190311802881d2802090c64de208651ba60454c8645bc631ca326699028ce326819884809cb484da2202043512d3c83121274c99c069c0468951128ca3106cd00489e2188518252148028e5ba80000300a0828015c820821194a41048a63408e99b48dd9222019102193c89108b28ddb840123b8444224700991412191290081085a200152102ec82811e346322035291a3346214906cb286c2327881b492918a54d92b488c002729234888a384283208a901840a3344e2248249344661cc349a1c410db020ce0220d8ac43041823114233213384a6112889aa40c02492dc14441cb966112368a40a2700c0709c842919120521839011b4669013244dcb89008212e53c091d2c4314c2046da3226ca204e438200c9b26dd89285d89490ca988c89c2684cb228842244114529cb4645c23846a1384a1b426d4a106de1926cdb2069d8182edc1211840284c49661d9a43162c02d43842d9a124d0ac12551202e61024021a848d0b80008012808181120c151d03486d34229c1286a1b00014346490098645340619c3260593082e228222287308b064802c66902114648846c083290a0c030104409dc348a4aa650c2928ce122908b362a10002a51b80d18492a0a21298c4230a4068e98420e1ac12501c93153222440086123c760e1103102c1205c341242386c24164e139424949205193408d94292e4c0442217461c408dc1021158c229a3c26552c872a9781e87970f539a3aa15e844ededa801e2fded4fdf54cd907ee3125287165e571ad52a2f7ea218d5c567f87642e64823ac57c4070f49ed557da35d0649ab7fd3f67194345932366e823ea13f18b44a00531482e805a813da0dd3e6834bc830647f62b81dfc94cee5861fdc6bc14e18b53f08895ae51ac5296a195f1419b7032931df15ec0a1f42c86a83b9de895b7b8361d307417f343a688e72ab677107eb496b63a781cdd1336c247d36b0c5f741a22553090e7597c0f1c150b1372da117e33d2d72890c67cfc08892b1c79d25cb737fd3c7b58c1f4b759f75e44e839f21785e1222973507e44a2c3d60551ccaed3d94d84fc488abc4d62d501dcda25fcb9b0b1b6b04f5794896880c63321a1aea08a0f3b67e0cb55efd12e2be0ec7c3e609b3086245245b2ccc1c41ff3afa7b6c706da5a460cfd13ce1fcc139b6bc82d0d69f754c03c0df310683f7bb23d2aeeee210dc0ca5778cb67eaa3422e0616613660e86f3db94d561c950fbcc3ff9e3c1836a565fd8627c8cc4bd6fa9bc2e52445b5c06a523d9863432f23691a6c8e592cdfb45c1911777a66bdfbcf59c540d5aef6732cb71ef8253e685c0229ea670f3b8b79128238abd2aee24ba593c58cbc9a386b0a2ae273e6c8da7721913a708630f9224385d2282cd83585f176348a48cd03ae14d81eddc420def6ca94bf5c575b8f38bb9065886a2ce996e2bb31876a8a74f109bc8df4cb610606212cdfea16bffb2962d7242ef3e4d01615ba4a76c386c76760c2fea0c0e47264151ae81ced36f5f65398f9a9c886cfaf2e9f29eb37323331128f76da15f22b0a28cabf82d0c53cdfb7933f1dc966ec713aee2c50ee453f9190e3644d338dc4323ffb3359dfc5e661c6e5aacd59cf0a79e05395d53ea2048c6b06fc78938a4cfe2945deaf2827cb81e5c898daccfbc288ecff381f8ceede9f612828a36f02676bf07a555b11ca284d7d54b545badeb7a353ac4036fefae62560aeec3a4f001d3a9c6f0de1dc39f85cdf5415eb65f8e0f979fa701721473387e8be42dcceb43ea8fe0a19d03396bfba2fcea8bd04480c2248936f044c1c25699ea82aa746e9660eb565ea48cce9306991876f37d7d6baa966c79e4436717e178b5dfd236f43e1e6f54be1ff9ed26584fb48479bb19ade0dd35000ddad5cf290c78e2eb71e350fb3b2b55f74ce23bdb6ed3bedfe229f37388ed3adbf52c09ca10a3d726f17213f530d486c5123931cb746030cb40d56e952175b90ca275abfc9c893d353c9b530920328ed20b4a09c6223bd20447b297d65932408081a87ffc96dfb9f3d47a0912d16e19b33943d8f89520c97ba04208d4ea059f99c828a04deb26e86761735d00f4d1bbd960dbc18db71b1c703835d9efec82b347e3a1507dc901e18fef44bed1d0be45d40bf793f0e91e17711ed76ffe95824283284bbdcadd9557f85de57783dd142dc11c5b27780305b7c617f0d5e91dbdd750d0e85142568e82d7c8211efbf8fc7c900377e5f73019a1c9037d23d177db1764318202819f59194b198f4065f0a49a83864a77c81ee0fd9698b00ccda5201bf6a512aef2c089ef5fab1b598ac3e144315f2164154b178e5262b7e177cf774abed522de59b1c5c0fe78f1d1ada73599b730122c62f2181e2bdbbb5a91ffb8e145e19d69961473d14e8459b4c1fc95d54ce4e622026625aae7dc95ff4e8bd900b4ace22eeaa48044d1467961e327393b030a00cc6ca94a4478d92f239dc26e65414e6ddc247aafbe4b091373007625d79862b10e51b5bee31f33b2ee04ed91fdff8ad7af0f9d0f83455f40c1ff03253b3bb17213c51b8d43d77a08f857353916bf1289b309de987491d47adde9820a0ead1143a18a83322744916d32341c464d7fb854082e3b67c9df49880ced0f924d228a53182eedf528e7173eb5a32ff513613fe6361ee8b57327bf843613a208979625325340e4728ba41ffc859c4063b4dee068987a82b5dab685c492b92a6ba21bb316183769f4e347c6beeec90627fb37cd83833381bc1243d887867189e1bb9af32dc011f34d4058bbf40ca6a7623e60f0521f9d69c4590c294efa9e4866c47d0edfacf5a316476ec0f7734ab5d7e53d4a03b8e0cfe8dcae5a5a8a21fbc6754c2494abfd93a38ad5c427fe2d021ac263fd1a022225fe6f21543179e7832e10d3b01dd806c6ab413a4c411287a829cd52a8fae72a73e5e46127ac1d26ea3a939d6b175113df75b7382dd3956902d7fecd195920ac34921b735dddeeb4daa248d34f38ade7046ebe26daabdae59f107a1ff7c1be752bdcc93dee3cc1df027f89fbb4eee6ad43fad2914069e936ccb3493");
        IO.println(Multibase.BASE_64_URL.encode(KeyCodec.MLDSA_44_PRIVATE_KEY.encode(d)));
    }

}
