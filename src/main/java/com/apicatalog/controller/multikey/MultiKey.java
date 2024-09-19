package com.apicatalog.controller.multikey;

import java.net.URI;
import java.time.Instant;

import com.apicatalog.ld.Term;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;
import com.apicatalog.linkedtree.literal.ByteArrayValue;
import com.apicatalog.multicodec.Multicodec;
import com.apicatalog.multicodec.MulticodecDecoder;

public class MultiKey implements KeyPair {

    protected static final String TYPE_NAME = "https://w3id.org/security#Multikey";
//    protected static final URI TYPE = URI.create(TYPE_NAME);

    protected URI id;
    protected URI controller;

    protected String algorithm;

    protected byte[] publicKey;
    protected byte[] privateKey;

    protected Instant revoked;
    protected Instant expiration;

    public static MultiKey of(
            MulticodecDecoder decoder,
            LinkedFragment source
            ) throws NodeAdapterError {

        final MultiKey multikey = new MultiKey();

        multikey.id = source.uri();
        multikey.controller = source.uri(MultiKeyAdapter.CONTROLLER.uri());

        var x = source.literal(MultiKeyAdapter.PUBLIC_KEY.uri(), ByteArrayValue.class);
        multikey.publicKey = getKey(MultiKeyAdapter.PUBLIC_KEY, x.byteArrayValue(), multikey, decoder);

//            multikey.privateKey = getKey(node, PRIVATE_KEY, multikey);
//
//            multikey.expiration = node.scalar(EXPIRATION).xsdDateTime();
//            multikey.revoked = node.scalar(REVOKED).xsdDateTime();
//
//        } else if (node.type().exists()) {
//            throw new DocumentError(ErrorType.Invalid, "VerificationMethodType");
//        }
//
//        validate(multikey);

//        return new LinkableObject(id, types, properties, multikey);
        return multikey;
    }

    @Override
    public byte[] publicKey() {
        return publicKey;
    }

    public void publicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public URI id() {
        return id;
    }

    public void id(URI id) {
        this.id = id;
    }

    @Override
    public String type() {
        return TYPE_NAME;
    }

    @Override
    public URI controller() {
        return controller;
    }

    public void controller(URI controller) {
        this.controller = controller;
    }

    @Override
    public byte[] privateKey() {
        return privateKey;
    }

    public void privateKey(byte[] privateKey) {
        this.privateKey = privateKey;
    }

    @Override
    public String algorithm() {
        return algorithm;
    }

    public void algorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public void revoked(Instant revoked) {
        this.revoked = revoked;
    }

    public Instant revoked() {
        return revoked;
    }

    public Instant expiration() {
        return expiration;
    }

    public void expiration(Instant expiration) {
        this.expiration = expiration;
    }

    protected static final byte[] getKey(Term term, final byte[] decodedKey, MultiKey multikey, MulticodecDecoder decoder) throws NodeAdapterError {

        if (decodedKey == null || decodedKey.length == 0) {
            return null;
        }

        final Multicodec codec = decoder.getCodec(decodedKey)
                .orElseThrow(() -> new NodeAdapterError("Invalid " + term.name() + " codec"));

        if (multikey.algorithm == null) {
            multikey.algorithm = getAlgorithmName(codec);

        } else if (!multikey.algorithm.equals(getAlgorithmName(codec))) {
            throw new NodeAdapterError("Invalid key pair codec [" + codec + "]");
        }

        return codec.decode(decodedKey);
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
    
    public static String typeName() {
        return TYPE_NAME;
    }
}
