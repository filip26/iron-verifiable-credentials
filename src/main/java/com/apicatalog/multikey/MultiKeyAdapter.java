package com.apicatalog.multikey;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.Term;
import com.apicatalog.ld.node.LdNode;
import com.apicatalog.ld.node.LdNodeBuilder;
import com.apicatalog.ld.node.LdScalar;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.ld.signature.key.VerificationKey;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multicodec.Multicodec;
import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.vc.VcVocab;
import com.apicatalog.vc.method.MethodAdapter;

import jakarta.json.JsonObject;

public abstract class MultiKeyAdapter implements MethodAdapter {

    public static final Term CONTROLLER = Term.create("controller", VcVocab.SECURITY_VOCAB);

    public static final Term PUBLIC_KEY = Term.create("publicKeyMultibase", VcVocab.SECURITY_VOCAB);
    public static final Term PRIVATE_KEY = Term.create("secretKeyMultibase", VcVocab.SECURITY_VOCAB);

    public static final Term EXPIRATION = Term.create("expiration", VcVocab.SECURITY_VOCAB);
    public static final Term REVOKED = Term.create("revoked", VcVocab.SECURITY_VOCAB);

    protected final MulticodecDecoder decoder;

    public MultiKeyAdapter(MulticodecDecoder decoder) {
        this.decoder = decoder;
    }

    protected abstract Multicodec getPublicKeyCodec(String algo, int keyLength);

    protected abstract Multicodec getPrivateKeyCodec(String algo, int keyLength);

    /**
     * Custom multikey validation.
     * 
     * @param method to validate
     * @throws DocumentError if there is validation error
     */
    protected void validate(MultiKey method) throws DocumentError {
        /* implement a custom validation */
    }

    @Override
    public VerificationMethod read(JsonObject document) throws DocumentError {
        if (document == null) {
            throw new IllegalArgumentException("Verification method cannot be null.");
        }

        final LdNode node = LdNode.of(document);

        final MultiKey multikey = new MultiKey();

        multikey.id = node.id();
        multikey.controller = node.node(CONTROLLER).id();

        if (node.type().hasType(MultiKey.TYPE.toString())) {

            multikey.publicKey = getKey(node, PUBLIC_KEY, multikey);
            multikey.privateKey = getKey(node, PRIVATE_KEY, multikey);

            multikey.expiration = node.scalar(EXPIRATION).xsdDateTime();
            multikey.revoked = node.scalar(REVOKED).xsdDateTime();

        } else if (node.type().exists()) {
            throw new DocumentError(ErrorType.Invalid, "VerificationMethodType");
        }

        validate(multikey);

        return multikey;
    }

    protected final byte[] getKey(final LdNode node, final Term term, final MultiKey multikey) throws DocumentError {

        final LdScalar key = node.scalar(term);

        if (key.exists()) {

            if (!"https://w3id.org/security#multibase".equals(key.type())) {
                throw new DocumentError(ErrorType.Invalid, term.name() + "Type");
            }

            final String encodedKey = key.string();

            if (!Multibase.BASE_58_BTC.isEncoded(encodedKey)) {
                throw new DocumentError(ErrorType.Invalid, term.name() + "Type");
            }

            final byte[] decodedKey = Multibase.BASE_58_BTC.decode(encodedKey);

            final Multicodec codec = decoder.getCodec(decodedKey).orElseThrow(() -> new DocumentError(ErrorType.Invalid, term.name() + "Codec"));

            if (multikey.algorithm == null) {
                multikey.algorithm = getAlgorithmName(codec);

            } else if (!multikey.algorithm.equals(getAlgorithmName(codec))) {
                throw new DocumentError(ErrorType.Invalid, "KeyPairCodec");
            }

            return codec.decode(decodedKey);
        }

        return null;
    }

    @Override
    public JsonObject write(VerificationMethod value) {

        LdNodeBuilder builder = new LdNodeBuilder();

        if (value.id() != null) {
            builder.id(value.id());
        }

        boolean embedded = false;

        if (value.controller() != null) {
            builder.set(CONTROLLER).id(value.controller());
            embedded = true;
        }

        if (value instanceof MultiKey) {
            MultiKey multikey = (MultiKey) value;
            if (multikey.getExpiration() != null) {
                builder.set(EXPIRATION).xsdDateTime(multikey.getExpiration());
                embedded = true;
            }
            if (multikey.getRevoked() != null) {
                builder.set(REVOKED).xsdDateTime(multikey.getRevoked());
                embedded = true;
            }
        }

        if (value instanceof VerificationKey) {
            VerificationKey verificationKey = (VerificationKey) value;

            if (verificationKey.publicKey() != null) {
                builder.set(PUBLIC_KEY)
                        .scalar("https://w3id.org/security#multibase",
                                Multibase.BASE_58_BTC.encode(
                                        getPublicKeyCodec(verificationKey.algorithm(), verificationKey.publicKey().length)
                                                .encode(verificationKey.publicKey())));
                ;
                embedded = true;
            }
        }

        if (value instanceof KeyPair) {
            KeyPair keyPair = (KeyPair) value;
            if (keyPair.privateKey() != null) {
                builder.set(PRIVATE_KEY)
                        .scalar("https://w3id.org/security#multibase",
                                Multibase.BASE_58_BTC.encode(
                                        getPrivateKeyCodec(keyPair.algorithm(), keyPair.privateKey().length)
                                                .encode(keyPair.privateKey())));
                ;
                embedded = true;
            }
        }

        if (embedded) {
            builder.type(value.type().toASCIIString());
        }

        return builder.build();
    }

    public static final String getAlgorithmName(Multicodec codec) {
        if (codec.name().endsWith("-priv")) {
            return codec.name().substring(0, codec.name().length() - "-priv".length()).toUpperCase();
        }
        if (codec.name().endsWith("-pub")) {
            return codec.name().substring(0, codec.name().length() - "-pub".length()).toUpperCase();
        }
        return codec.name().toUpperCase();
    }
}
