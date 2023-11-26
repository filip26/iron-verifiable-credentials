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
import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.vc.integrity.DataIntegrityVocab;

import jakarta.json.JsonObject;

public class MultiKey implements KeyPair {

    protected static final URI TYPE = URI.create("https://w3id.org/security#Multikey");

//    protected static final URI CONTEXT = URI.create("https://w3id.org/security/multikey/v1");

//    protected static final String MULTIKEY_VOCAB = ""; // FIXME

//    protected static final LdTerm PUBLIC_KEY = LdTerm.create("", MULTIKEY_VOCAB);

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
        multikey.publicKey = updateKey(node, DataIntegrityVocab.MULTIBASE_PUB_KEY, multikey);
        multikey.privateKey = updateKey(node, DataIntegrityVocab.MULTIBASE_PRIV_KEY, multikey);
        
        return multikey;
    }

    protected static final byte[] updateKey(final LdNode node, final LdTerm term, final MultiKey multikey) throws DocumentError {
        
        final LdScalar key = node.get(term).scalar();
        
        if (key.exists()) {
            if (!key.type().hasType("https://w3id.org/security#multibase")) {
                throw new DocumentError(ErrorType.Invalid, term.name());
            }
            final String encodedKey = key.string();
            if (!Multibase.BASE_58_BTC.isEncoded(encodedKey)) {
                throw new DocumentError(ErrorType.Invalid, term.name() + "Type");
            }

            final byte[] decodedKey = Multibase.BASE_58_BTC.decode(encodedKey);

            final Multicodec codec = MULTICODEC.getCodec(decodedKey).orElseThrow(() -> new DocumentError(ErrorType.Invalid, term.name() + "Codec"));

            if (multikey.keyType == null) {
                multikey.keyType = getKeyTypeName(codec);

            } else if (!multikey.keyType.equals(getKeyTypeName(codec))) {
                throw new DocumentError(ErrorType.Invalid, "KeyPairCodec");
            }

            return codec.decode(decodedKey);
        }
        
        return null;
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
