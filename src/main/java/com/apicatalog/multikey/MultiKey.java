package com.apicatalog.multikey;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.Term;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.node.LdNode;
import com.apicatalog.ld.node.LdScalar;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.linkedtree.Link;
import com.apicatalog.linkedtree.Linkable;
import com.apicatalog.linkedtree.LinkedContainer;
import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedTree;
import com.apicatalog.linkedtree.literal.ByteArrayValue;
import com.apicatalog.linkedtree.primitive.LinkableObject;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multicodec.Multicodec;
import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.vc.lt.MultibaseLiteral;
import com.apicatalog.vc.lt.ObjectFragmentMapper;
import com.apicatalog.vcdi.DataIntegrityVocab;

public class MultiKey implements KeyPair {

    protected static final URI TYPE = URI.create("https://w3id.org/security#Multikey");

    protected URI id;
    protected URI controller;

    protected String algorithm;

    protected byte[] publicKey;
    protected byte[] privateKey;

    protected Instant revoked;
    protected Instant expiration;

    public static LinkedFragment of(
            Link id,
            Collection<String> types,
            Map<String, LinkedContainer> properties,
            Supplier<LinkedTree> rootSupplier,
            MulticodecDecoder decoder
            ) throws DocumentError {

        // TODO

        final ObjectFragmentMapper selector = new ObjectFragmentMapper(properties);

        final MultiKey multikey = new MultiKey();

        multikey.id = selector.id(id);
        multikey.controller = selector.id(MultiKeyAdapter.CONTROLLER);

        var x = selector.single(MultiKeyAdapter.PUBLIC_KEY, ByteArrayValue.class);
        System.out.println(x);

        multikey.publicKey = getKey(MultiKeyAdapter.PUBLIC_KEY, x.byteArrayValue(), multikey, decoder);
        System.out.println(multikey.publicKey);
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

        return new LinkableObject(id, types, properties, rootSupplier, multikey);
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
    public URI type() {
        return TYPE;
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

    protected static final byte[] getKey(Term term, final byte[] decodedKey, MultiKey multikey, MulticodecDecoder decoder) throws DocumentError {

        if (decodedKey == null || decodedKey.length == 0) {
            return null;
        }
        
        final Multicodec codec = decoder.getCodec(decodedKey).orElseThrow(() -> new DocumentError(ErrorType.Invalid, term.name() + "Codec"));

        if (multikey.algorithm == null) {
            multikey.algorithm = getAlgorithmName(codec);

        } else if (!multikey.algorithm.equals(getAlgorithmName(codec))) {
            throw new DocumentError(ErrorType.Invalid, "KeyPairCodec");
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
}
