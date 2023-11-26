package com.apicatalog.multikey;

import java.net.URI;

import com.apicatalog.jsonld.JsonLdReader;
import com.apicatalog.jsonld.schema.LdTerm;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.node.LdNode;
import com.apicatalog.ld.node.LdScalar;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multicodec.Multicodec;
import com.apicatalog.multicodec.Multicodec.Tag;
import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.vc.integrity.DataIntegrityVocab;

import jakarta.json.JsonObject;

public class MultiKey implements KeyPair {

    protected static final URI TYPE = URI.create("https://w3id.org/security#Multikey");

    protected static final URI CONTEXT = URI.create("https://w3id.org/security/multikey/v1");

    protected static final String MULTIKEY_VOCAB = ""; // FIXME

    protected static final LdTerm PUBLIC_KEY = LdTerm.create("", MULTIKEY_VOCAB);

    protected static final MulticodecDecoder MULTICODEC = MulticodecDecoder.getInstance();

    protected URI id;
    protected URI controller;
    protected String keyType;
    protected byte[] publicKey;
    protected byte[] privateKey;

    @Override
    public byte[] publicKey() {
        return publicKey();
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public URI id() {
        return id;
    }

    public void setId(URI id) {
        this.id = id;
    }

    @Override
    public URI type() {
        return TYPE;
    }

    @Override
    public URI controller() {
        return controller;
    }

    public void setController(URI controller) {
        this.controller = controller;
    }

    @Override
    public byte[] privateKey() {
        return privateKey;
    }

    public void setPrivateKey(byte[] privateKey) {
        this.privateKey = privateKey;
    }

    public static VerificationMethod readMethod(JsonObject document) throws DocumentError {

        if (document == null) {
            throw new IllegalArgumentException("Verification method cannot be null.");
        }

        if (!JsonLdReader.isTypeOf(TYPE.toString(), document)) {
//            throw new DocumentError(ErrorType.Invalid, ");
        }

        final LdNode node = new LdNode(document);

        final MultiKey multikey = new MultiKey();

        multikey.id = node.id();
        multikey.controller = node.get(DataIntegrityVocab.CONTROLLER).id();

        final LdScalar publicKey = node.get(DataIntegrityVocab.MULTIBASE_PUB_KEY).scalar();
        if (publicKey.exists()) {
            if (!publicKey.type().hasType("https://w3id.org/security#multibase")) {
                throw new DocumentError(ErrorType.Invalid, DataIntegrityVocab.MULTIBASE_PUB_KEY.name());
            }
            final String encodedPublicKey = publicKey.string();
            if (!Multibase.BASE_58_BTC.isEncoded(encodedPublicKey)) {
                throw new DocumentError(ErrorType.Invalid, DataIntegrityVocab.MULTIBASE_PUB_KEY.name() + "Type");
            }

            final byte[] decodedPublicKey = Multibase.BASE_58_BTC.decode(encodedPublicKey);

            final Multicodec codec = MULTICODEC.getCodec(decodedPublicKey).orElseThrow(() -> new DocumentError(ErrorType.Invalid, DataIntegrityVocab.MULTIBASE_PUB_KEY.name() + "Codec"));
System.out.println("P " + codec);
            multikey.keyType = getKeyTypeName(codec);
            multikey.publicKey = codec.decode(decodedPublicKey);
        }


        final LdScalar privateKey = node.get(DataIntegrityVocab.MULTIBASE_PRIV_KEY).scalar();
        if (privateKey.exists()) {
            if (!privateKey.type().hasType("https://w3id.org/security#multibase")) {
                throw new DocumentError(ErrorType.Invalid, DataIntegrityVocab.MULTIBASE_PRIV_KEY.name());
            }
            final String encodedPrivateKey = privateKey.string();
            if (!Multibase.BASE_58_BTC.isEncoded(encodedPrivateKey)) {
                throw new DocumentError(ErrorType.Invalid, DataIntegrityVocab.MULTIBASE_PRIV_KEY.name() + "Type");
            }

            final byte[] decodedPrivateKey = Multibase.BASE_58_BTC.decode(encodedPrivateKey);

            final Multicodec codec = MULTICODEC.getCodec(decodedPrivateKey).orElseThrow(() -> new DocumentError(ErrorType.Invalid, DataIntegrityVocab.MULTIBASE_PUB_KEY.name() + "Codec"));
            System.out.println("- " + codec);
            multikey.privateKey = codec.decode(decodedPrivateKey);
        }

        return multikey;
    }

    @Override
    public String keyType() {
        return keyType;
    }

    protected static final String getKeyTypeName(Multicodec codec) {
        if (codec.name().endsWith("-priv")) {
            return codec.name().substring(0, codec.name().length() - "-priv".length()).toUpperCase();
        }
        if (codec.name().endsWith("-pub")) {
            return codec.name().substring(0, codec.name().length() - "-pub".length()).toUpperCase();
        }
        return codec.name().toUpperCase();
    }

    // TODO revoked
}
