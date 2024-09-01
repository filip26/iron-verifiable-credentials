package com.apicatalog.multikey;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.linkedtree.Link;
import com.apicatalog.linkedtree.Linkable;
import com.apicatalog.linkedtree.LinkedContainer;
import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedTree;
import com.apicatalog.linkedtree.primitive.LinkableObject;

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
            Supplier<LinkedTree> rootSupplier) {

        // TODO

        final MultiKey multikey = new MultiKey();

//        multikey.id = URI.create(id);
//        multikey.controller = prop 
//
//        multikey.id = node.id();
//        multikey.controller = node.node(CONTROLLER).id();
//
//        if (node.type().hasType(MultiKey.TYPE.toString())) {
//
//            multikey.publicKey = getKey(node, PUBLIC_KEY, multikey);
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

}
